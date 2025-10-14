package com.ssginc.showpingrefactoring.common.jwt;

import com.ssginc.showpingrefactoring.common.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    public static final String ATTR_AUTH_ERROR_CODE = "AUTH_ERROR_CODE"; // 엔트리포인트에서 읽어감
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = extractAccessTokenFromCookie(request);

        if (token != null) {
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    // 유효 → 인증 세팅
                    UsernamePasswordAuthenticationToken auth = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else if (jwtTokenProvider.isExpired(token)) {
                    // 만료 → 에러 코드 태깅 (엔트리포인트가 401 + {"code":"AT_EXPIRED"}로 응답)
                    request.setAttribute(ATTR_AUTH_ERROR_CODE, ErrorCode.ACCESS_TOKEN_EXPIRED);
                } else {
                    // 기타 유효성 실패 (변조 등)
                    request.setAttribute(ATTR_AUTH_ERROR_CODE, ErrorCode.INVALID_ACCESS_TOKEN);
                }
            } catch (ExpiredJwtException ex) {
                // 파서 단계에서 바로 만료 예외가 난 경우
                request.setAttribute(ATTR_AUTH_ERROR_CODE, com.ssginc.showpingrefactoring.common.exception.ErrorCode.ACCESS_TOKEN_EXPIRED);
            } catch (JwtException | IllegalArgumentException ex) {
                request.setAttribute(ATTR_AUTH_ERROR_CODE, com.ssginc.showpingrefactoring.common.exception.ErrorCode.INVALID_ACCESS_TOKEN);
            }
        }
        // token == null 인 경우: 게스트 접근 허용 → 아무 것도 세팅하지 않고 통과

        chain.doFilter(request, response);
    }

    private String extractAccessTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(c -> "accessToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
