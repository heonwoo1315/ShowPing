package com.ssginc.showpingrefactoring.common.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;


public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Spring Security가 만들어 둔 CSRF 토큰을 요청 attribute에서 꺼냄
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (token != null) {
            String newValue = token.getToken();
            var existing = WebUtils.getCookie(request, "XSRF-TOKEN");
            String oldValue = existing != null ? existing.getValue() : null;

            // 쿠키가 없거나 값이 바뀌었으면 재발급
            if (oldValue == null || !oldValue.equals(newValue)) {
                ResponseCookie cookie = ResponseCookie.from("XSRF-TOKEN", newValue)
                        .httpOnly(false)                // JS에서 읽어 헤더로 보낼 수 있어야 함
                        .secure(request.isSecure())     // 운영 HTTPS면 true, 로컬 HTTP면 false
                        .sameSite("Lax")
                        .path("/")
                        .build();
                response.addHeader("Set-Cookie", cookie.toString());
            }
        }

        filterChain.doFilter(request, response);
    }
}
