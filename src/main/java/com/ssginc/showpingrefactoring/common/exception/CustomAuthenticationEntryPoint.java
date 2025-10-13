package com.ssginc.showpingrefactoring.common.exception;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssginc.showpingrefactoring.common.dto.CustomErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        // JwtFilter가 심어둔 에러코드 사용
        Object attr = request.getAttribute(com.ssginc.showpingrefactoring.common.jwt.JwtFilter.ATTR_AUTH_ERROR_CODE);
        ErrorCode ec = (attr instanceof ErrorCode) ? (ErrorCode) attr : ErrorCode.AUTH_UNAUTHORIZED;
        // API 요청이면 JSON, 그 외엔 그대로 로그인으로 보내되, 최소한 API는 정확한 코드를 내려줌
        boolean isApi = request.getRequestURI().startsWith("/api");
        if (isApi) {
            response.setStatus(ec.getStatus().value());
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("X-Error-Code", ec.getCode());
            String body = "{\"code\":\"" + ec.getCode() + "\",\"message\":\"" + ec.getMessage() + "\"}";
            response.getWriter().write(body);
            } else {
            response.sendRedirect("/login");
            }
    }
}
