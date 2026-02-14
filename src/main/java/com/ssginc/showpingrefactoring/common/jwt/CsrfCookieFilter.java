package com.ssginc.showpingrefactoring.common.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
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

        // Spring Security가 심어둔 토큰을 두 키 모두에서 시도해 가져온다.
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token == null) {
            token = (CsrfToken) request.getAttribute("_csrf");
        }

        if (token != null) {
            String newValue = token.getToken();
            var existing = WebUtils.getCookie(request, "XSRF-TOKEN");
            String oldValue = existing != null ? existing.getValue() : null;

            if (oldValue == null || !oldValue.equals(newValue)) {
                ResponseCookie cookie = ResponseCookie.from("XSRF-TOKEN", newValue)
                        .httpOnly(false)          // JS에서 읽어 헤더로 보낼 수 있어야 함
                        .secure(true)       // 운영(https)=true, 로컬(http)=false
                        .sameSite("Lax")
                        .path("/")
                        .build();
                response.addHeader("Set-Cookie", cookie.toString());
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSecureRequest(HttpServletRequest req) {
        // 리버스 프록시 뒤라면 X-Forwarded-Proto 기준으로 판단
        String xfp = req.getHeader("X-Forwarded-Proto");
        if (xfp != null) return "https".equalsIgnoreCase(xfp);
        return req.isSecure();
    }
}
