package com.ssginc.showpingrefactoring.domain.member.service;

public interface RedisTokenService {
    void saveRefreshToken(String memberId, String refreshToken);
    String getRefreshToken(String memberId);
    void deleteRefreshToken(String memberId);
    boolean validateRefreshToken(String memberId, String refreshToken);
    void deleteRefreshTokenByRt(String refreshToken);
    boolean rotateRefreshToken(String memberId, String oldRt, String newRt);
}
