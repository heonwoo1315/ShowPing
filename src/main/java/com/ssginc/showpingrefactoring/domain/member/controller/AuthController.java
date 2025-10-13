package com.ssginc.showpingrefactoring.domain.member.controller;

import com.ssginc.showpingrefactoring.common.exception.CustomException;
import com.ssginc.showpingrefactoring.common.util.CookieUtil;
import com.ssginc.showpingrefactoring.domain.member.dto.request.LoginRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.response.LoginResponseDto;
import com.ssginc.showpingrefactoring.domain.member.dto.response.TokenResponseDto;
import com.ssginc.showpingrefactoring.domain.member.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;
    /**
     * 로그인
     */
    @Operation(summary = "로그인", description = "ID와 Password로 로그인하고 Access/Refresh Token을 HTTPOnly 쿠키로 설정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "필수 파라미터 누락"),
            @ApiResponse(responseCode = "401", description = "로그인 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request, HttpServletResponse response) {
        Map<String, String> tokens = authService.login(request);

        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        // AT/RT 쿠키 설정 (로그아웃 시 RT 쿠키를 삭제할 수 있도록 RT도 쿠키로 설정)
        ResponseCookie accessTokenCookie = cookieUtil.createCookie("accessToken", accessToken, 3600);
        ResponseCookie refreshTokenCookie = cookieUtil.createCookie("refreshToken", refreshToken, 86400);

        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        // LoginResponseDto (status만 남은 DTO)를 반환
        return ResponseEntity.ok(new LoginResponseDto(tokens.get("status")));
    }

    // (추가) 토큰 재발급 엔드포인트
    @Operation(summary = "Access Token 재발급", description = "Refresh Token 쿠키를 이용하여 새로운 Access Token과 Refresh Token을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "Refresh Token 유효성 검증 실패 (재로그인 필요)")
    })
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.extractTokenFromCookie(request, "refreshToken");

        if (refreshToken == null) {
            cookieUtil.clearAuthCookies(response);
            return ResponseEntity.status(com.ssginc.showpingrefactoring.common.exception.ErrorCode.INVALID_REFRESH_TOKEN.getStatus())
                    .header("X-Error-Code", com.ssginc.showpingrefactoring.common.exception.ErrorCode.INVALID_REFRESH_TOKEN.getCode())
                    .body(Map.of("code", com.ssginc.showpingrefactoring.common.exception.ErrorCode.INVALID_REFRESH_TOKEN.getCode()));
        }

        try {
            // Service에서 String[] {newAccessToken, newRefreshToken} 반환
            String[] newTokens = authService.reissue(refreshToken);
            String newAccessToken = newTokens[0];
            String newRefreshToken = newTokens[1];

            // ✅ 새로운 AT와 RT 모두 쿠키로 설정 (RT Rotation)
            ResponseCookie newAccessTokenCookie = cookieUtil.createCookie("accessToken", newAccessToken, 3600);
            ResponseCookie newRefreshTokenCookie = cookieUtil.createCookie("refreshToken", newRefreshToken, 86400);

            response.addHeader("Set-Cookie", newAccessTokenCookie.toString());
            response.addHeader("Set-Cookie", newRefreshTokenCookie.toString());

            // 토큰을 포함하지 않고 성공 상태만 반환
            return ResponseEntity.ok(new TokenResponseDto());

        } catch (CustomException e) {
            // RT 검증 실패 시: RT 삭제 및 AT, RT 쿠키 모두 제거 (강제 로그아웃 유도)
            cookieUtil.clearAuthCookies(response);
            return ResponseEntity.status(com.ssginc.showpingrefactoring.common.exception.ErrorCode.INVALID_REFRESH_TOKEN.getStatus())
                    .header("X-Error-Code", com.ssginc.showpingrefactoring.common.exception.ErrorCode.INVALID_REFRESH_TOKEN.getCode())
                    .body(Map.of(
                            "code", com.ssginc.showpingrefactoring.common.exception.ErrorCode.INVALID_REFRESH_TOKEN.getCode(),
                            "message", com.ssginc.showpingrefactoring.common.exception.ErrorCode.INVALID_REFRESH_TOKEN.getMessage()
                    ));
        }
    }

    @Operation(summary = "로그아웃", description = "Redis에서 Refresh Token을 삭제하고, Access Token 및 Refresh Token 쿠키를 제거합니다.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "로그아웃 성공"))
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // refreshToken 쿠키 기준으로 Redis에서 삭제
        String refreshToken = cookieUtil.extractTokenFromCookie(request, "refreshToken");
        if (refreshToken != null) {
            authService.logoutByRefreshToken(refreshToken);
        }

        // ✅ AT와 RT 쿠키 모두 제거 (기존의 AT 쿠키만 제거하는 로직을 대체)
        cookieUtil.clearAuthCookies(response);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "로그인 사용자 정보 조회", description = "현재 로그인한 사용자의 username과 role을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        // ❗인증이 없거나(=null) 익명 토큰이면 Security EntryPoint로 넘김
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new InsufficientAuthenticationException("Unauthenticated");
        }

        Object principal = authentication.getPrincipal();

        String username = (principal instanceof UserDetails ud)
                ? ud.getUsername()
                : String.valueOf(principal);

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // e.g. ROLE_ADMIN
                .findFirst()
                .orElse(null);

        String roleSimple = (role != null && role.startsWith("ROLE_"))
                ? role.substring(5)
                : role;

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "username", username,
                "role", roleSimple
        ));
    }
}

