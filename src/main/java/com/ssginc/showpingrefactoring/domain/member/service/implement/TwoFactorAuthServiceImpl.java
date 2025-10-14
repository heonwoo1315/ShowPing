package com.ssginc.showpingrefactoring.domain.member.service.implement;

import com.ssginc.showpingrefactoring.common.exception.CustomException;
import com.ssginc.showpingrefactoring.common.exception.ErrorCode;
import com.ssginc.showpingrefactoring.common.util.EncryptionUtil;
import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.domain.member.repository.MemberRepository;
import com.ssginc.showpingrefactoring.domain.member.service.TwoFactorAuthService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.IGoogleAuthenticator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorAuthServiceImpl implements TwoFactorAuthService {
    private final MemberRepository memberRepository;

    // 🔹 GoogleAuthenticator 설정
    private final IGoogleAuthenticator googleAuthenticator = new GoogleAuthenticator(
            new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                    .setWindowSize(1) // 30초 내 코드만 허용
                    .build()
    );

    @Override
    public boolean verifyTotpCode(String memberId, int totpCode) {
        Member admin = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (admin.getOtpSecretKey() == null) {
            throw new CustomException(ErrorCode.TOTP_NOT_REGISTERED);
        }

        // 🔹 OTP SecretKey 복호화
        String decryptedSecretKey = EncryptionUtil.decrypt(admin.getOtpSecretKey());
        log.info("복호화된 OTP SecretKey: {}", decryptedSecretKey);

        // 🔹 TOTP 코드 검증
        boolean isValid = googleAuthenticator.authorize(decryptedSecretKey, totpCode);

        if (!isValid) {
            throw new CustomException(ErrorCode.INVALID_TOTP_CODE);
        }

        return true;
    }
}
