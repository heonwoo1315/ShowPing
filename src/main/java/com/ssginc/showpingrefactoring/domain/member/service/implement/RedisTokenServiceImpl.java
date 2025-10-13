package com.ssginc.showpingrefactoring.domain.member.service.implement;

import com.ssginc.showpingrefactoring.common.jwt.JwtTokenProvider;
import com.ssginc.showpingrefactoring.domain.member.service.RedisTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisTokenServiceImpl implements RedisTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redis;

    // yml 파일의 jwt.refresh.expiration 값을 주입받아 사용
    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpirationMillis;

    private String hashRt(String rt) {
        // 권장: HMAC-SHA256(secret, rt). 예시는 간단히 MD5로 표기(실서비스에선 SHA-256/HMAC 쓰세요!)
        return DigestUtils.md5DigestAsHex(rt.getBytes(StandardCharsets.UTF_8));
    }
    private String keyRt(String rt) {            // rt:{hash} -> memberId
        return "rt:" + hashRt(rt);
    }
    private String keyMemberSet(String memberId) { // member:{id}:rts (Set of rt hashes)
        return "member:" + memberId + ":rts";
    }

    /**
     * RefreshToken 저장
     * @param memberId - 사용자 식별자
     * @param refreshToken - 발급된 RefreshToken
     */
    @Override
    public void saveRefreshToken(String memberId, String refreshToken) {

        // 1) 1인1RT 매핑
        redisTemplate.opsForValue().set(buildKey(memberId), refreshToken,
                refreshTokenExpirationMillis, TimeUnit.MILLISECONDS);

        // 2) 인덱스 + 세트(단건 역조회/정리용)
        String rtKey  = keyRt(refreshToken);                // "rt:" + hash(rt)
        String setKey = keyMemberSet(memberId);             // "member:" + memberId + ":rts"
        redis.opsForValue().set(rtKey, memberId, refreshTokenExpirationMillis, TimeUnit.MILLISECONDS);
        redis.opsForSet().add(setKey, rtKey.substring(3));
    }

    /**
     * RefreshToken 가져오기
     * @param memberId - 사용자 식별자
     * @return 저장된 RefreshToken or null
     */
    @Override
    public String getRefreshToken(String memberId) {
        return redisTemplate.opsForValue().get(buildKey(memberId));
    }

    /**
     * RefreshToken 삭제
     */
    //  logout 메서드 수정: Redis 조회 전 JWT 유효성 검증 추가
    @Override
    public void deleteRefreshToken(String refreshToken) {
        String memberId = null;
        try {
            // 유효한 토큰이면 정상 파싱
            if (jwtTokenProvider.validateToken(refreshToken)) {
                memberId = jwtTokenProvider.getUsername(refreshToken);
            }
        } catch (io.jsonwebtoken.ExpiredJwtException eje) {
            memberId = eje.getClaims().getSubject(); // 만료여도 subject 가능
        } catch (Exception ignore) { /* 완전 손상 시 삭제 불가 → 조용히 무시 */ return; }

        if (memberId != null) {
            redisTemplate.delete(buildKey(memberId));
        }
    }

    /**
     * RefreshToken 존재 및 일치 여부 검사
     * @param memberId - 사용자 식별자
     * @param refreshToken - 요청으로 들어온 RefreshToken
     * @return 일치하면 true, 아니면 false
     */
    @Override
    public boolean validateRefreshToken(String memberId, String refreshToken) {
        String storedToken = getRefreshToken(memberId);
        return storedToken != null && storedToken.equals(refreshToken);
    }

    @Override
    public void deleteRefreshTokenByRt(String refreshToken) {
        // 1) 인덱스 기반 삭제: rt:{hash(rt)} -> memberId
        String rtKey = keyRt(refreshToken);          // 예: "rt:" + hash(rt)
        String memberId = redis.opsForValue().get(rtKey);
        if (memberId != null) {
            String rtHash = rtKey.substring(3);      // "rt:" 제거(너의 규약을 따른다면)
            // 1-1) 사용자 세트에서 제거
            redis.opsForSet().remove(keyMemberSet(memberId), rtHash);
            // 1-2) 인덱스 키 삭제
            redis.delete(rtKey);
            // 1-3) 1인1RT 매핑(refresh:{memberId})가 이 RT라면 그것도 삭제
            String current = redisTemplate.opsForValue().get(buildKey(memberId));
            if (refreshToken.equals(current)) {
                redisTemplate.delete(buildKey(memberId));
            }
            return;
        }

        // 2) 폴백: 토큰에서 memberId(subject) 추출해 삭제 (만료 RT도 가능)
        String mid = null;
        try {
            if (jwtTokenProvider.validateToken(refreshToken)) {
                mid = jwtTokenProvider.getUsername(refreshToken);
            }
        } catch (io.jsonwebtoken.ExpiredJwtException eje) {
            mid = eje.getClaims().getSubject();
        } catch (Exception ignore) { /* 완전 손상 → 아무 것도 못 함 */ }

        if (mid != null) {
            // 2-1) refresh:{memberId}가 이 RT라면 삭제
            String current = redisTemplate.opsForValue().get(buildKey(mid));
            if (refreshToken.equals(current)) {
                redisTemplate.delete(buildKey(mid));
            }
            // 2-2) (있다면) 세트/인덱스도 정리
            String rtHash = keyRt(refreshToken).substring(3);
            redis.opsForSet().remove(keyMemberSet(mid), rtHash);
            redis.delete(rtKey); // 혹시 남아 있다면
        }
    }

    @Override
    public boolean rotateRefreshToken(String memberId, String oldRt, String newRt) {
        String stored = getRefreshToken(memberId);
        if (stored == null || !stored.equals(oldRt)) return false;

        // 1) 1인1RT 갱신
        redisTemplate.opsForValue().set(buildKey(memberId), newRt,
                refreshTokenExpirationMillis, TimeUnit.MILLISECONDS);

        // 2) 인덱스 교체
        String oldRtKey = keyRt(oldRt), newRtKey = keyRt(newRt);
        String setKey   = keyMemberSet(memberId);
        redis.delete(oldRtKey);
        redis.opsForSet().remove(setKey, oldRtKey.substring(3));
        redis.opsForValue().set(newRtKey, memberId, refreshTokenExpirationMillis, TimeUnit.MILLISECONDS);
        redis.opsForSet().add(setKey, newRtKey.substring(3));

        return true;
    }

    /**
     * Redis에 저장할 Key 포맷
     */
    private String buildKey(String memberId) {
        return "refresh:" + memberId;
    }

}
