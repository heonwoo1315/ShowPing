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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "회원 로그인", description = "ID와 비밀번호로 로그인 후 AccessToken과 RefreshToken을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "필수 파라미터 누락"),
            @ApiResponse(responseCode = "401", description = "로그인 실패")
    })
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

    @Operation(summary = "회원 로그아웃", description = "AccessToken을 무효화하여 로그아웃 처리합니다.")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
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
