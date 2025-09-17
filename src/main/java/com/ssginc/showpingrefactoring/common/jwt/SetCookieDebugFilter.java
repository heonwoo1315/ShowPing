package com.ssginc.showpingrefactoring.common.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//커밋 메시지 새로 작성
@Component
@Order(1) // 예외핸들 전/후 어디든 보이게 우선순위 높게
public class SetCookieDebugFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(res) {
            @Override public void addCookie(Cookie cookie) {
                if ("XSRF-TOKEN".equalsIgnoreCase(cookie.getName())) {
                    new Exception("[DEBUG] addCookie XSRF-TOKEN path=" + cookie.getPath()
                            + " maxAge=" + cookie.getMaxAge()).printStackTrace();
                }
                super.addCookie(cookie);
            }

            @Override public void addHeader(String name, String value) {
                if ("Set-Cookie".equalsIgnoreCase(name) &&
                        value != null && value.startsWith("XSRF-TOKEN=")) {
                    new Exception("[DEBUG] addHeader Set-Cookie -> " + value).printStackTrace();
                }
                super.addHeader(name, value);
            }
        };

        chain.doFilter(req, wrapper);
    }
}
