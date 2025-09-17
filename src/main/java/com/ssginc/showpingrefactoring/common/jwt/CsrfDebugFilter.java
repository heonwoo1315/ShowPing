package com.ssginc.showpingrefactoring.common.jwt;

import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

// 커밋 메시지 새로 작성

public class CsrfDebugFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CsrfDebugFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        String hdr = req.getHeader("X-XSRF-TOKEN");

        String cookie = Optional.ofNullable(req.getCookies())
                .stream().flatMap(Arrays::stream)
                .filter(c -> "XSRF-TOKEN".equals(c.getName()))
                .map(jakarta.servlet.http.Cookie::getValue)
                .findFirst().orElse(null);

        CsrfToken serverToken = (CsrfToken) req.getAttribute(CsrfToken.class.getName());

        log.info("[CSRF DEBUG] {} {} | header(X-XSRF-TOKEN)={} | cookie(XSRF-TOKEN)={} | serverToken={}",
                req.getMethod(),
                req.getRequestURI(),
                mask(hdr),
                mask(cookie),
                serverToken != null ? mask(serverToken.getToken()) : "null");

        chain.doFilter(req, res);
    }

    private String mask(String token) {
        if (token == null) return "null";
        if (token.length() <= 8) return "****";
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
}
