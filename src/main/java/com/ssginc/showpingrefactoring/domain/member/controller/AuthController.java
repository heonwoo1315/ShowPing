package com.ssginc.showpingrefactoring.domain.member.controller;

import com.ssginc.showpingrefactoring.common.jwt.JwtTokenProvider;
import com.ssginc.showpingrefactoring.common.jwt.RedisTokenService;
import com.ssginc.showpingrefactoring.domain.member.dto.request.LoginRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.response.LoginResponseDto;
import com.ssginc.showpingrefactoring.domain.member.dto.request.ReissueRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.response.TokenResponseDto;
import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.domain.member.service.AuthService;
import com.ssginc.showpingrefactoring.domain.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenService redisTokenService;

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        String memberId = request.get("memberId");
        String password = request.get("password");

        if (memberId == null || password == null) {
            return ResponseEntity.status(400).body(Map.of("status", "BAD_REQUEST", "message", "Missing required parameters"));
        }

        Member member = memberService.findMember(memberId, password);

        if (member != null) {
            String role = member.getMemberRole().name();
            String accessToken = jwtTokenProvider.generateAccessToken(memberId, role);
            String refreshToken = jwtTokenProvider.generateRefreshToken(memberId);
            redisTokenService.saveRefreshToken(memberId, refreshToken);

            // ✅ 무조건 로그인 성공
            return ResponseEntity.ok(Map.of(
                    "memberRole", role,
                    "accessToken", accessToken,
                    "refreshToken", refreshToken
            ));
        }

        return ResponseEntity.status(401).body(Map.of("status", "LOGIN_FAILED"));
    }

    /**
     * AccessToken 재발급
     */
//    @PostMapping("/reissue")
//    public ResponseEntity<TokenResponseDto> reissue(@RequestBody ReissueRequestDto request) {
//        TokenResponseDto response = authService.reissue(request);
//        return ResponseEntity.ok(response);
//    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        // Authorization 헤더에서 AccessToken 추출
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String memberId = authService.getMemberIdFromToken(token);
            authService.logout(memberId);
        }

        return ResponseEntity.ok().build();
    }
}
