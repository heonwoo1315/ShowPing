package com.ssginc.showpingrefactoring.domain.member.controller;

import com.ssginc.showpingrefactoring.common.util.CookieUtil;
import com.ssginc.showpingrefactoring.common.util.MfaPrincipal;
import com.ssginc.showpingrefactoring.common.util.MfaPrincipalExtractor;
import com.ssginc.showpingrefactoring.common.util.RateLimiter;
import com.ssginc.showpingrefactoring.domain.member.entity.AdminDevice;
import com.ssginc.showpingrefactoring.domain.member.entity.AdminDeviceStatus;
import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.domain.member.repository.AdminDeviceRepository;
import com.ssginc.showpingrefactoring.domain.member.repository.MemberRepository;
import com.ssginc.showpingrefactoring.domain.member.service.InviteService;
import com.ssginc.showpingrefactoring.domain.member.service.TokenService;
import com.ssginc.showpingrefactoring.domain.member.service.TotpService;
import com.ssginc.showpingrefactoring.domain.member.service.WebAuthnService;
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
                         CookieUtil cookieUtil) {
        this.invite = invite;
        this.webauthn = webauthn;
        this.totp = totp;
        this.tokens = tokens;
        this.rate = rate;
        this.extractor = extractor;
        this.redis = redis;
        this.memberRepo = memberRepo;
        this.adminDeviceRepository = adminDeviceRepository;
        this.cookieUtil = cookieUtil;
    }

    private MfaPrincipal me(Authentication auth) {
        return extractor.extract(auth).orElseThrow(() -> new IllegalStateException("No principal"));
    }

    // (기존 관리자 초대 API) MFA + ADMIN 동시 보유 필요
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

    // 등록 완료(간이: attestation 검증 스킵, credentialId만 저장)
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

            // 프론트가 id/rawId 중 무엇을 보내든 수용
            String rawIdB64 = Objects.toString(req.getOrDefault("rawId", req.get("id")), null);
            if (rawIdB64 == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("code", "BAD_REQUEST", "message", "rawId 없음"));
            }
            byte[] rawId = b64url(rawIdB64);

            AdminDevice device = new AdminDevice();
            device.setId(UUID.randomUUID());
            Member member = memberRepo.getReferenceById(me.getMemberNo());
            device.setMember(member);
            device.setCredentialId(rawId);      // VARBINARY 컬럼
            device.setPublicKeyCose(new byte[0]);
            device.setStatus(AdminDeviceStatus.APPROVED);
            device.setCreatedAt(Instant.now());
            adminDeviceRepository.save(device);

            String secret = totp.issueSecret(me.getMemberNo());
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
    @GetMapping("/assertion/options")
    public Map<String, Object> assertionOptions(Authentication authentication) {
        var me = me(authentication);
        return webauthn.assertionOptions(me.getMemberNo());
    }

    @PostMapping("/verify")
    @ResponseBody
    public Map<String, Object> verify(Authentication authentication,
                                      @RequestBody Map<String, String> req,
                                      HttpServletResponse response) throws Exception {

        var me = me(authentication);
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
        tokens.rotateRefresh(me.getMemberNo());

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
