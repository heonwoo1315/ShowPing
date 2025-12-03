package com.ssginc.showpingrefactoring.domain.member.service.implement;

import com.ssginc.showpingrefactoring.domain.member.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class InviteServiceImpl implements InviteService {

    private final StringRedisTemplate redis;

    @Value("${redis.prefix:sp:mfa:}") private String prefix;
    @Value("${mfa.inviteTtlSeconds:600}") private int ttlSeconds;

    private String key(String token){ return prefix + "invite:" + token; }

    private String randomUrlSafe() {
        byte[] b = new byte[16];
        new SecureRandom().nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    @Override
    public String issue(Long memberNo){
        String token = randomUrlSafe();
        // 초대는 단일 사용자에게 1회성으로, TTL 부여
        redis.opsForValue().set(key(token), String.valueOf(memberNo), Duration.ofSeconds(ttlSeconds));
        return token;
    }

    @Override
    public void validate(Long memberNo, String token){
        String v = redis.opsForValue().get(key(token));
        if (v == null) throw new IllegalArgumentException("Invite expired/invalid");
        if (!v.equals(String.valueOf(memberNo))) throw new AccessDeniedException("Invite not for this user");
        // 여기서는 삭제하지 않음: options/attest 두 스텝을 위해 남겨두고,
        // TOTP 검증 완료 시 complete()에서 삭제
    }

    @Override
    public void complete(String inviteId) {
        redis.delete(key(inviteId));
    }
}
