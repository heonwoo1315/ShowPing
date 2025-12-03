package com.ssginc.showpingrefactoring.domain.member.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssginc.showpingrefactoring.common.util.CookieUtil;
import com.ssginc.showpingrefactoring.common.util.MfaPrincipal;
import com.ssginc.showpingrefactoring.common.util.MfaPrincipalExtractor;
import com.ssginc.showpingrefactoring.common.util.RateLimiter;
import com.ssginc.showpingrefactoring.domain.member.entity.AdminDevice;
import com.ssginc.showpingrefactoring.domain.member.entity.AdminDeviceStatus;
import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.domain.member.repository.AdminDeviceRepository;
import com.ssginc.showpingrefactoring.domain.member.repository.MemberMfaRepository;
import com.ssginc.showpingrefactoring.domain.member.repository.MemberRepository;
import com.ssginc.showpingrefactoring.domain.member.service.InviteService;
import com.ssginc.showpingrefactoring.domain.member.service.TokenService;
import com.ssginc.showpingrefactoring.domain.member.service.TotpService;
import com.ssginc.showpingrefactoring.domain.member.service.WebAuthnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/auth/mfa")
public class MfaController {

    private final InviteService invite;
    private final WebAuthnService webauthn;
    private final TotpService totp;
    private final TokenService tokens;
    private final RateLimiter rate;
    private final MfaPrincipalExtractor extractor;

    private final StringRedisTemplate redis;
    private final MemberRepository memberRepo;
    private final AdminDeviceRepository adminDeviceRepository;
    private final CookieUtil cookieUtil;
    private final MemberMfaRepository memberMfaRepository;
    private final ObjectMapper objectMapper;

    @Value("${webauthn.rp.id:localhost}")
    private String rpId;

    @Value("${redis.prefix:sp:mfa:}")
    private String redisPrefix;

    @Value("${mfa.allowSelfInvite:false}")
    private boolean allowSelfInvite;

    @Value("${mfa.mfaMaxAgeSeconds:900}")
    private long mfaMaxAgeSeconds;


    public MfaController(InviteService invite,
                         WebAuthnService webauthn,
                         TotpService totp,
                         TokenService tokens,
                         RateLimiter rate,
                         MfaPrincipalExtractor extractor,
                         StringRedisTemplate redis,
                         MemberRepository memberRepo,
                         AdminDeviceRepository adminDeviceRepository,
                         MemberMfaRepository memberMfaRepository,
                         CookieUtil cookieUtil,
                         ObjectMapper objectMapper) {
        this.invite = invite;
        this.webauthn = webauthn;
        this.totp = totp;
        this.tokens = tokens;
        this.rate = rate;
        this.extractor = extractor;
        this.redis = redis;
        this.memberRepo = memberRepo;
        this.adminDeviceRepository = adminDeviceRepository;
        this.memberMfaRepository = memberMfaRepository;
        this.cookieUtil = cookieUtil;
        this.objectMapper = objectMapper;
    }

    private MfaPrincipal me(Authentication auth) {
        return extractor.extract(auth).orElseThrow(() -> new IllegalStateException("No principal"));
    }

    // (기존 관리자 초대 API) MFA + ADMIN 동시 보유 필요
    @Operation(
            summary = "관리자 MFA 등록 초대 발급",
            description = "MFA 권한 및 ADMIN 역할을 모두 가진 관리자가 자신의 계정에 대한 MFA 등록 초대 코드를 발급합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "초대 코드 발급 성공"),
            @ApiResponse(responseCode = "403", description = "본인 외 계정 초대 시 거부"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PreAuthorize("hasAuthority('MFA') and hasRole('ADMIN')")
    @PostMapping("/enroll/invite")
    public Map<String, String> invite(@RequestBody Map<String, Long> req, Authentication auth) {
        var me = me(auth);
        Long target = req.get("memberNo");
        if (target == null || !target.equals(me.getMemberNo())) {
            throw new AccessDeniedException("Only self invite is allowed");
        }
        return Map.of("inviteId", invite.issue(me.getMemberNo()));
    }

    // 최초 부트스트랩 또는 정책 허용 시 자기 자신 초대
    @Operation(
            summary = "자기 자신 초대 발급",
            description = "최초 관리자 단말 등록 또는 정책상 허용된 경우, 관리자 본인에 대한 MFA 등록 초대 코드를 발급합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "초대 코드 발급 성공"),
            @ApiResponse(responseCode = "403", description = "Self-invite 비허용 또는 MFA 재인증 필요"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/enroll/invite/me")
    public Map<String, String> inviteMe(Authentication auth) {
        var me = me(auth);

        boolean hasApproved = adminDeviceRepository
                .existsByMember_MemberNoAndStatus(me.getMemberNo(), AdminDeviceStatus.APPROVED);

        if (!hasApproved) {
            return Map.of("inviteId", invite.issue(me.getMemberNo()));
        }
        if (!allowSelfInvite) throw new AccessDeniedException("Self-invite disabled");
        if (!hasFreshMfa(me)) throw new AccessDeniedException("MFA required");

        return Map.of("inviteId", invite.issue(me.getMemberNo()));
    }

    private boolean hasFreshMfa(MfaPrincipal p) {
        Long ts = p.getMfaTs();
        if (ts == null) return false;
        long age = Instant.now().getEpochSecond() - ts;
        return age <= mfaMaxAgeSeconds;
    }

    // 등록 옵션(브라우저 -> navigator.credentials.create)
    @Operation(
            summary = "WebAuthn 등록 옵션 조회",
            description = "초대 코드를 검증한 후, 관리자 단말 등록을 위한 WebAuthn PublicKeyCredential 생성 옵션을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 옵션 조회 성공"),
            @ApiResponse(responseCode = "400", description = "초대 코드(inviteId) 누락 또는 유효하지 않음"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "초대 대상 불일치")
    })
    @GetMapping("/enroll/options")
    public Map<String, Object> enrollOptions(Authentication authentication, @RequestParam String inviteId) {
        var me = me(authentication);
        invite.validate(me.getMemberNo(), inviteId);

        String chal = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));

        redis.opsForValue().set(redisPrefix + "webauthn:regchal:" + me.getMemberNo(),
                chal, Duration.ofSeconds(120));

        return Map.of(
                "challenge", chal,
                "rp", Map.of("id", rpId, "name", "ShowPing Admin MFA"),
                "user", Map.of(
                        "id", Base64.getUrlEncoder().withoutPadding()
                                .encodeToString(String.valueOf(me.getMemberNo()).getBytes(StandardCharsets.UTF_8)),
                        "name", String.valueOf(me.getMemberNo()),
                        "displayName", "Admin #" + me.getMemberNo()
                ),
                "pubKeyCredParams", List.of(
                        Map.of("type", "public-key", "alg", -7),    // ES256
                        Map.of("type", "public-key", "alg", -257)   // RS256
                ),
                "authenticatorSelection", Map.of(
                        "authenticatorAttachment", "platform",
                        "residentKey", "preferred",
                        "userVerification", "preferred"
                ),
                "attestation", "none"
        );
    }

    private static byte[] b64url(String s) {
        try { return Base64.getUrlDecoder().decode(s); }
        catch (IllegalArgumentException e) {
            String p = s.replace('-', '+').replace('_', '/');
            switch (p.length() % 4) { case 2 -> p += "=="; case 3 -> p += "="; }
            return Base64.getDecoder().decode(p);
        }
    }

    // 아래 메서드에 challenge 문자열 추출
    private String extractChallengeFromClientData(byte[] clientDataJSON) {
        try {
            String json = new String(clientDataJSON, StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(json);
            return node.get("challenge").asText();
        } catch (Exception e) {
            throw new IllegalStateException("clientDataJSON 파싱 실패", e);
        }
    }

    // 등록 완료(간이: attestation 검증 스킵, credentialId만 저장)
    @Operation(
            summary = "WebAuthn 등록 완료(Attestation)",
            description = "브라우저에서 생성된 WebAuthn 등록 결과(rawId 등)를 받아 관리자 단말 정보를 저장하고, TOTP 시크릿(otpauth URL)을 발급합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 완료 및 TOTP 정보 발급"),
            @ApiResponse(responseCode = "400", description = "inviteId 또는 rawId 누락"),
            @ApiResponse(responseCode = "403", description = "초대 검증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/enroll/attest")
    public ResponseEntity<?> enrollAttest(Authentication authentication, @RequestBody Map<String, Object> req) {
        try {
            var me = me(authentication);

            String inviteId = Objects.toString(req.get("inviteId"), null);
            if (inviteId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("code", "BAD_REQUEST", "message", "inviteId 없음"));
            }
            invite.validate(me.getMemberNo(), inviteId);

            // 1. 클라이언트가 보낸 clientDataJSON(Base64URL) 추출
            String clientDataJSONB64 = Objects.toString(req.get("clientDataJSON"), null);
            if (clientDataJSONB64 == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("code", "BAD_REQUEST", "message", "clientDataJSON 없음"));
            }
            byte[] clientDataJSON = b64url(clientDataJSONB64);

            // 2) Redis에서 등록용 regchal 조회
            String key = redisPrefix + "webauthn:regchal:" + me.getMemberNo();
            String expectedChal = redis.opsForValue().get(key);
            if (expectedChal == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("code", "CHALLENGE_EXPIRED", "message", "등록용 challenge가 만료되었거나 존재하지 않습니다."));
            }

            // 3) clientDataJSON 안 challenge 추출
            String clientChallenge = extractChallengeFromClientData(clientDataJSON);

            // 4) regchal 은 한 번만 사용하도록 즉시 삭제
            redis.delete(key);

            // 5) 서버가 발급한 regchal과 일치하는지 확인
            if (!expectedChal.equals(clientChallenge)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("code", "CHALLENGE_MISMATCH", "message", "등록용 WebAuthn challenge 불일치"));
            }

            // 프론트가 id/rawId 중 무엇을 보내든 수용
            String rawIdB64 = Objects.toString(req.getOrDefault("rawId", req.get("id")), null);
            if (rawIdB64 == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("code", "BAD_REQUEST", "message", "rawId 없음"));
            }
            byte[] rawId = b64url(rawIdB64);

            AdminDevice device = new AdminDevice();
            // device.setId(UUID.randomUUID());
            Member member = memberRepo.getReferenceById(me.getMemberNo());
            device.setMember(member);
            device.setCredentialId(rawId);      // VARBINARY 컬럼
            device.setPublicKeyCose(new byte[0]);
            device.setStatus(AdminDeviceStatus.APPROVED);
            device.setCreatedAt(Instant.now());
            adminDeviceRepository.save(device);

            String secret = totp.issueSecret(me.getMemberNo());

            var mfa = memberMfaRepository.findById(me.getMemberNo())
                    .orElseThrow(() -> new IllegalStateException("MFA row not found after issueSecret"));
            mfa.setBoundDevice(device);
            memberMfaRepository.save(mfa);

            String otpauth = "otpauth://totp/ShowPing:" + me.getMemberNo()
                    + "?secret=" + secret + "&issuer=ShowPing&digits=6&period=30";

            return ResponseEntity.ok(Map.of(
                    "deviceId", device.getId().toString(),
                    "otpauth", otpauth
            ));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("code", "FORBIDDEN", "message", e.getMessage()));
        } catch (Exception e) {
            // 서버 로그로 원인 파악
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", "INTERNAL_SERVER_ERROR", "message", "알 수 없는 서버 오류가 발생했습니다."));
        }
    }

    // 등록 검증(TOTP)
    @Operation(
            summary = "MFA 등록 검증(TOTP)",
            description = "관리자 단말 등록 후, 발급된 TOTP 코드로 MFA 등록을 최종 검증하고 초대 상태를 완료로 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "TOTP 검증 및 초대 완료 성공"),
            @ApiResponse(responseCode = "400", description = "totp 또는 inviteId 누락"),
            @ApiResponse(responseCode = "401", description = "TOTP 검증 실패 또는 인증 실패"),
            @ApiResponse(responseCode = "429", description = "Rate Limit 초과(요청 제한)")
    })
    @PostMapping("/enroll/verify")
    public ResponseEntity<?> enrollVerify(Authentication authentication, @RequestBody Map<String, String> req) {
        var me = me(authentication);
        rate.check("enroll:" + me.getMemberNo());

        String totpCode = req.get("totp");
        if (totpCode == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("code", "BAD_REQUEST", "message", "totp 없음"));
        }
        totp.verify(me.getMemberNo(), totpCode);

        String inviteId = Objects.toString(req.get("inviteId"), "");
        invite.complete(inviteId);

        return ResponseEntity.ok(Map.of("ok", true));
    }

    // (참고) 인증 옵션/검증 — 기존 로직 유지
    @Operation(
            summary = "WebAuthn 인증 옵션 조회",
            description = "관리자 로그인을 위한 WebAuthn assertion 옵션(인증기 선택, challenge 등)을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 옵션 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/assertion/options")
    public Map<String, Object> assertionOptions(Authentication authentication) {
        var me = me(authentication);
        return webauthn.assertionOptions(me.getMemberNo());
    }

    @Operation(
            summary = "MFA 인증(WebAuthn + TOTP)",
            description = "WebAuthn assertion과 TOTP 코드를 검증한 후, 기존 Refresh Token을 로테이션하고 MFA 정보가 포함된 Access/Refresh Token을 재발급하여 쿠키에 설정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MFA 인증 및 토큰 재발급 성공"),
            @ApiResponse(responseCode = "400", description = "요청 파라미터 누락 또는 형식 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패(WebAuthn/TOTP)"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "429", description = "Rate Limit 초과(요청 제한)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/verify")
    @ResponseBody
    public Map<String, Object> verify(Authentication authentication,
                                      @RequestBody Map<String, String> req,
                                      HttpServletResponse response) throws Exception {

        var me = me(authentication);
        String memberId = authentication.getName();

        rate.check("verify:" + me.getMemberNo());

        // 1) WebAuthn assertion 검증 (네 기존 로직 유지)
        var deviceId = webauthn.verifyAssertion(
                me.getMemberNo(),
                b64url(req.get("rawId")),
                b64url(req.get("authenticatorData")),
                b64url(req.get("clientDataJSON")),
                b64url(req.get("signature"))
        );

        // 2) TOTP 검증
        totp.verify(me.getMemberNo(), req.get("totp"));

        // 3) RT 로테이션(권장)
        tokens.rotateRefresh(memberId);

        // 4) 최종 AT/RT 발급 (mfa=true, mfa_ts, device_id 클레임 포함)
        var t = tokens.issueMfaTokens(authentication.getPrincipal(), Map.of(
                "mfa", true,
                "mfa_ts", java.time.Instant.now().getEpochSecond(),
                "device_id", deviceId.toString()
        ));
        String at = String.valueOf(t.get("accessToken"));
        String rt = String.valueOf(t.get("refreshToken"));

        // 5) 쿠키 세팅 (만료시간은 정책에 맞춰 조정)
        response.addHeader("Set-Cookie", cookieUtil.createCookie("accessToken", at, 3600).toString());
        response.addHeader("Set-Cookie", cookieUtil.createCookie("refreshToken", rt, 86400).toString());

        // 6) 프런트가 즉시 관리자 메인으로 이동하도록 힌트 반환
        return Map.of("ok", true, "redirect", "/admin/", "mfa", true);
    }

}
