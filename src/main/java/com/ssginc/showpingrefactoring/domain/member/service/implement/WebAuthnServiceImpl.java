package com.ssginc.showpingrefactoring.domain.member.service.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssginc.showpingrefactoring.domain.member.entity.AdminDeviceStatus;
import com.ssginc.showpingrefactoring.domain.member.repository.AdminDeviceRepository;
import com.ssginc.showpingrefactoring.domain.member.service.WebAuthnService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WebAuthnServiceImpl implements WebAuthnService {

    private final AdminDeviceRepository deviceRepo;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    @Value("${redis.prefix:sp:mfa:}") private String prefix;
    @Value("${mfa.webauthnChallengeTtlSeconds:120}") private int challengeTtl;
    @Value("${webauthn.rp.id:localhost}") private String rpId;

    @Override
    public Map<String, Object> assertionOptions(Long memberNo) {
        byte[] challenge = UUID.randomUUID().toString().getBytes();
        String chal = Base64.getUrlEncoder().withoutPadding().encodeToString(challenge);
        redis.opsForValue().set(prefix + "webauthn:chal:" + memberNo, chal, Duration.ofSeconds(challengeTtl));

        var allow = new ArrayList<Map<String,Object>>();
        deviceRepo.findAllByMember_MemberNoAndStatus(memberNo, AdminDeviceStatus.APPROVED)
                .forEach(d -> allow.add(Map.of(
                        "type","public-key",
                        "id", Base64.getUrlEncoder().withoutPadding().encodeToString(d.getCredentialId())
                )));

        return Map.of(
                "challenge", chal,
                "rpId", rpId,
                "allowCredentials", allow,
                "userVerification", "preferred"
        );
    }

    @Override
    public UUID verifyAssertion(Long memberNo, byte[] rawId, byte[] authenticatorData, byte[] clientDataJSON, byte[] signature) {
        // TODO: webauthn4j 로 검증 교체
        // 1) Redis에 저장된 로그인용 challenge(chal) 조회
        String key = prefix + "webauthn:chal:" + memberNo;
        String expected = redis.opsForValue().get(key);
        if (expected == null) {
            throw new IllegalStateException("로그인용 WebAuthn challenge가 만료되었거나 존재하지 않습니다.");
        }

        // 2) clientDataJSON 안의 challenge 추출
        String clientChallenge = extractChallengeFromClientData(clientDataJSON);

        // 3) challenge는 한 번만 사용하도록 즉시 삭제 (재전송 공격 방지)
        redis.delete(key);

        // 4) 서버가 발급한 challenge와 클라이언트가 서명한 challenge가 같은지 확인
        if (!expected.equals(clientChallenge)) {
            throw new IllegalArgumentException("로그인용 WebAuthn challenge 불일치");
        }

        // 5) 나머지 기존 검증 로직(디바이스 바인딩 검증)
        var dev = deviceRepo.findByCredentialId(rawId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown device"));
        if (!Objects.equals(dev.getMember().getMemberNo(), memberNo))
            throw new IllegalArgumentException("Device not bound to member");
        if (dev.getStatus() != AdminDeviceStatus.APPROVED)
            throw new IllegalStateException("Device not approved");

        dev.setLastSeenAt(Instant.now());
        deviceRepo.save(dev);
        return dev.getId();
    }

    private String extractChallengeFromClientData(byte[] clientDataJSON) {
        try {
            String json = new String(clientDataJSON, StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(json);

            // WebAuthn 스펙상 clientDataJSON.challenge 는 base64url 문자열
            return node.get("challenge").asText();
        } catch (Exception e) {
            throw new IllegalStateException("clientDataJSON 파싱 실패", e);
        }
    }
}
