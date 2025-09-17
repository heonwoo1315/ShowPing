package com.ssginc.showpingrefactoring.common.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//커밋 메시지 새로 작성
/**
 * Spring Security 6.x에서 CSRF 토큰이 지연 생성되는 문제를 보완하는 필터.
 * 매 요청마다 CsrfToken을 한 번 "터치"해서 반드시 생성/동기화되게 만든다.
 */
public class CsrfInitFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token == null) {
            token = (CsrfToken) request.getAttribute("_csrf");
        }
        if (token != null) {
            token.getToken(); // touch → 실제 토큰 값 초기화/생성
        }

        filterChain.doFilter(request, response);
    }
}