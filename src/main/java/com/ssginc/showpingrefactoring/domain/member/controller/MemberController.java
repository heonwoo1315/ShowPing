package com.ssginc.showpingrefactoring.domain.member.controller;

import com.ssginc.showpingrefactoring.domain.member.dto.*;
import com.ssginc.showpingrefactoring.common.jwt.JwtTokenProvider;
import com.ssginc.showpingrefactoring.domain.member.service.MailService;
import com.ssginc.showpingrefactoring.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final Map<String, Boolean> verifiedEmailStorage = new HashMap<>();
    private final MailService mailService;

    /**
     * 회원 가입
     */
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody SignupRequestDto request) {
        memberService.signup(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 회원 정보 조회 (내 정보)
     */
    @GetMapping("/me")
    public ResponseEntity<MemberDto> getMyInfo(HttpServletRequest request) {
        String memberId = getMemberIdFromRequest(request);
        MemberDto memberDto = memberService.getMemberInfo(memberId);
        return ResponseEntity.ok(memberDto);
    }

    /**
     * 회원 정보 수정
     */
    @PutMapping("/me")
    public ResponseEntity<Void> updateMyInfo(@RequestBody UpdateMemberRequestDto request, HttpServletRequest httpRequest) {
        String memberId = getMemberIdFromRequest(httpRequest);
        memberService.updateMember(memberId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 회원 탈퇴
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(HttpServletRequest request) {
        String memberId = getMemberIdFromRequest(request);
        memberService.deleteMember(memberId);
        return ResponseEntity.ok().build();
    }

    /**
     * AccessToken에서 memberId 추출 (Authorization 헤더 사용)
     */
    private String getMemberIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // 여기는 JwtTokenProvider를 직접 주입하거나, 서비스에서 따로 빼야 해
            // 여기서는 MemberController에 JwtTokenProvider 추가 주입한다고 가정할게
            return jwtTokenProvider.getUsername(token);
        }
        throw new RuntimeException("No Authorization Header Found");
    }

    @PostMapping("/email/send-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody EmailRequestDto request) {
        mailService.sendSignupVerificationCode(request.getEmail());
        return ResponseEntity.ok("인증 코드가 이메일로 발송되었습니다.");
    }

    @PostMapping("/email/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody EmailVerifyRequestDto request) {
        boolean isValid = mailService.verifySignupCode(request.getEmail(), request.getCode());

        if (isValid) {
            // 이메일 인증 성공 기록 (임시 저장소)
            verifiedEmailStorage.put(request.getEmail(), true);
            return ResponseEntity.ok("인증 성공");
        } else {
            return ResponseEntity.badRequest().body("인증 실패: 잘못된 코드입니다.");
        }
    }
}
