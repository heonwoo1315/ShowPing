package com.ssginc.showpingrefactoring.common.exception;


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

        String accept = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");

        boolean isApiRequest = accept != null && accept.contains("application/json") ||
                requestedWith != null && requestedWith.equalsIgnoreCase("XMLHttpRequest") ||
                request.getRequestURI().startsWith("/api");

        if (isApiRequest) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"message\": \"Unauthorized access. Please login.\"}");
        } else {
            response.sendRedirect("/login");
        }
    }
}
