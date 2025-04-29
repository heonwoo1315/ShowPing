package com.ssginc.showpingrefactoring.domain.member.service;

public interface TwoFactorAuthService {
    boolean verifyTotpCode(String memberId, int totpCode);
}
