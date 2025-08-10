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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request,
                                                     HttpServletRequest httpReq,
                                                     HttpServletResponse httpRes) {
        String memberId = request.get("memberId");
        String password = request.get("password");
        if (memberId == null || password == null) {
            return ResponseEntity.status(400).body(Map.of("status", "BAD_REQUEST", "message", "Missing required parameters"));
        }

        Member member = memberService.findMember(memberId, password);
        if (member == null) {
            return ResponseEntity.status(401).body(Map.of("status", "LOGIN_FAILED"));
        }

        String role = member.getMemberRole().name();
        String accessToken = jwtTokenProvider.generateAccessToken(memberId, role);
        String refreshToken = jwtTokenProvider.generateRefreshToken(memberId);
        redisTokenService.saveRefreshToken(memberId, refreshToken);

        // 로컬(http)만 secure=false, 운영(https) secure=true
        String host = httpReq.getServerName();
        boolean isLocal = "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(!isLocal)          // ← 로컬은 false
                .sameSite("Lax")
                .path("/")
                .maxAge(60 * 30)
                .build();

        // ❗ addHeader 대신, ResponseEntity에 직접 헤더를 얹어 리턴
        return ResponseEntity.ok()
                .header("Set-Cookie", accessCookie.toString())
                .body(Map.of("memberRole", role));
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
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // Authorization 헤더에서 AccessToken 추출
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null) {
            String memberId = authService.getMemberIdFromToken(token);
            authService.logout(memberId);
        }

        ResponseCookie cookie = ResponseCookie.from("accessToken", null)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "로그인 사용자 정보 조회", description = "현재 로그인한 사용자의 username과 role을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "status", "UNAUTHORIZED"
            ));
        }

        UserDetails user = (UserDetails) authentication.getPrincipal();
        String username = user.getUsername();
        String role = user.getAuthorities().stream().findFirst().get().getAuthority(); // ex: ROLE_ADMIN

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "username", username,
                "role", role.replace("ROLE_", "") // "ROLE_ADMIN" → "ADMIN"
        ));
    }
}
