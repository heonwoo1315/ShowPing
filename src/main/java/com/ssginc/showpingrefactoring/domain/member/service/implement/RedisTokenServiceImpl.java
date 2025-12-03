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
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Override
    public void deleteAllRefreshTokens(String memberId) {
        // member:{memberId}:rts (Set Key)
        String setKey = keyMemberSet(memberId);

        // 1. Set에 저장된 모든 해시값을 가져옴
        Set<String> rtHashes = redis.opsForSet().members(setKey);

        if (rtHashes != null && !rtHashes.isEmpty()) {
            // 2. 각 해시값에 해당하는 rt:{hash} 인덱스 키 목록 생성 및 삭제
            // (rt: 프리픽스는 set에 저장된 해시값에는 포함되어 있지 않으므로 수동으로 추가)
            List<String> rtKeysToDelete = rtHashes.stream()
                    .map(hash -> "rt:" + hash)
                    .collect(Collectors.toList());

            if (!rtKeysToDelete.isEmpty()) {
                redis.delete(rtKeysToDelete); // rt:해쉬값1, rt:해쉬값2, ... 모두 삭제
            }
        }

        // 3. refresh:{memberId} 키 삭제 (현재 유효한 1인1RT 키)
        redisTemplate.delete(buildKey(memberId));

        // 4. member:{memberId}:rts Set 자체 삭제
        redis.delete(setKey);
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
