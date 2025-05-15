package com.ssginc.showpingrefactoring.domain.member.controller;

import com.ssginc.showpingrefactoring.common.jwt.JwtTokenProvider;
import com.ssginc.showpingrefactoring.common.jwt.RedisTokenService;
import com.ssginc.showpingrefactoring.common.util.EncryptionUtil;
import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.domain.member.repository.MemberRepository;
import com.ssginc.showpingrefactoring.domain.member.service.TwoFactorAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/2fa")
@RequiredArgsConstructor
public class TwoFactorAuthController {

    private final TwoFactorAuthService twoFactorAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenService redisTokenService;
    private final MemberRepository memberRepository;


    /**
     * TOTP 2FA 인증
     */
    @PostMapping("/verify")
    public ResponseEntity<String> verifyTotp(@RequestParam String memberId, @RequestParam int code) {
        twoFactorAuthService.verifyTotpCode(memberId, code);

        String accessToken = jwtTokenProvider.generateAccessToken(memberId, "ROLE_ADMIN");
        String refreshToken = jwtTokenProvider.generateRefreshToken(memberId);
        redisTokenService.saveRefreshToken(memberId, refreshToken);

        return ResponseEntity.ok("2FA 인증 성공\nAccessToken: " + accessToken + "\nRefreshToken: " + refreshToken);
    }

    @GetMapping("/setup")
    public ResponseEntity<String> getTotpSetup(@RequestParam String memberId) {
        Member admin = memberRepository.findByMemberId(memberId).orElse(null);
        if (admin == null) {
            return ResponseEntity.badRequest().body("관리자 계정이 존재하지 않습니다.");
        }

        if (admin.getOtpSecretKey() == null) {
            return ResponseEntity.badRequest().body("OTP 키가 설정되지 않았습니다.");
        }

        String decryptedSecretKey = EncryptionUtil.decrypt(admin.getOtpSecretKey());
        String issuer = "ShowPing";
        String qrCodeUrl = "otpauth://totp/" + issuer + ":" + admin.getMemberId() +
                "?secret=" + decryptedSecretKey + "&issuer=" + issuer;

        return ResponseEntity.ok(qrCodeUrl);
    }
}
