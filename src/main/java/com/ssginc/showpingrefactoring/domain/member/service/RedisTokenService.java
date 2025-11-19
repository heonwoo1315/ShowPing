package com.ssginc.showpingrefactoring.domain.member.service;

public interface RedisTokenService {
    void saveRefreshToken(String memberId, String refreshToken);
    String getRefreshToken(String memberId);
    void deleteRefreshToken(String memberId);
    boolean rotateRefreshToken(String memberId, String oldRt, String newRt);
    void deleteAllRefreshTokens(String memberId);
}
