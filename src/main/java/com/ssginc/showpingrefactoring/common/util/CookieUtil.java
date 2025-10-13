package com.ssginc.showpingrefactoring.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
    // 공통 쿠키 생성 유틸리티
     public ResponseCookie createCookie(String name, String value, long maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false) // TODO: HTTPS 환경에서는 true로 변경
                .path("/")
                .sameSite("Lax")
                .maxAge(maxAge)
                .build();
    }

    public void clearAuthCookies(HttpServletResponse response) {
        response.addHeader("Set-Cookie", createCookie("accessToken", null, 0).toString());
        response.addHeader("Set-Cookie", createCookie("refreshToken", null, 0).toString());
        response.addHeader("Set-Cookie", createCookie("XSRF-TOKEN", null, 0).toString());
    }

    public String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
