package com.ssginc.showpingrefactoring.common.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    // 🔹 Refresh Token 저장 (7일 설정 예시)
    private static final long REFRESH_TOKEN_EXPIRATION_MINUTES = 7 * 24 * 60; // 7일

    /**
     * RefreshToken 저장
     * @param memberId - 사용자 식별자
     * @param refreshToken - 발급된 RefreshToken
     */
    public void saveRefreshToken(String memberId, String refreshToken) {
        redisTemplate.opsForValue()
                .set(buildKey(memberId), refreshToken, REFRESH_TOKEN_EXPIRATION_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * RefreshToken 가져오기
     * @param memberId - 사용자 식별자
     * @return 저장된 RefreshToken or null
     */
    public String getRefreshToken(String memberId) {
        return redisTemplate.opsForValue().get(buildKey(memberId));
    }

    /**
     * RefreshToken 삭제
     * @param memberId - 사용자 식별자
     */
    public void deleteRefreshToken(String memberId) {
        redisTemplate.delete(buildKey(memberId));
    }

    /**
     * RefreshToken 존재 및 일치 여부 검사
     * @param memberId - 사용자 식별자
     * @param refreshToken - 요청으로 들어온 RefreshToken
     * @return 일치하면 true, 아니면 false
     */
    public boolean validateRefreshToken(String memberId, String refreshToken) {
        String storedToken = getRefreshToken(memberId);
        return storedToken != null && storedToken.equals(refreshToken);
    }

    /**
     * Redis에 저장할 Key 포맷
     */
    private String buildKey(String memberId) {
        return "refresh:" + memberId;
    }

//    private final RedisTemplate<String, String> redisTemplate;
//    private static final long REFRESH_TOKEN_EXPIRATION = 86400000L; // 24시간
//
//    // Refresh Token 저장
//    public void saveRefreshToken(String username, String refreshToken) {
//
//        System.out.println("refreshToken 저장 단계 : \"" + username +"\"");
//        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
//        valueOps.set("refreshToken:" + username, refreshToken, REFRESH_TOKEN_EXPIRATION, TimeUnit.MILLISECONDS);
//    }
//
//    // Refresh Token 조회
//    public String getRefreshToken(String username) {
//        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
//        return (String) valueOps.get("refreshToken:" + username);
//    }
//
//    // Refresh Token 삭제 (로그아웃 시 사용)
//    public void deleteRefreshToken(String username) {
//        System.out.println("Redis에서 Refresh Token 삭제 요청: " + username);
//        redisTemplate.delete("refreshToken:" + username);
//        System.out.println("Redis에서 Refresh Token 삭제 완료!");
//    }
//
//    public String checkRefreshToken(String username) {
//        String token = getRefreshToken(username);
//        System.out.println("Redis에 저장된 Refresh Token: " + token);
//        return token;
//    }
}
