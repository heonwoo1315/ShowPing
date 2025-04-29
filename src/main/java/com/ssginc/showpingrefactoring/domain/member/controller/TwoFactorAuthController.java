package com.ssginc.showpingrefactoring.domain.member.controller;

import com.ssginc.showpingrefactoring.domain.member.service.TwoFactorAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/2fa")
@RequiredArgsConstructor
public class TwoFactorAuthController {

    private final TwoFactorAuthService twoFactorAuthService;

    /**
     * TOTP 2FA 인증
     */
    @PostMapping("/verify")
    public ResponseEntity<String> verifyTotp(@RequestParam String memberId, @RequestParam int code) {
        twoFactorAuthService.verifyTotpCode(memberId, code);
        return ResponseEntity.ok("2FA 인증 성공");
    }
}
