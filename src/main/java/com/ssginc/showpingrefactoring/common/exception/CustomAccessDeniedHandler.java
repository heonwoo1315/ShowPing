package com.ssginc.showpingrefactoring.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssginc.showpingrefactoring.common.dto.CustomErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {

        String accept = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");
        boolean isApiRequest =
                (accept != null && accept.contains("application/json")) ||
                        (requestedWith != null && requestedWith.equalsIgnoreCase("XMLHttpRequest")) ||
                        request.getRequestURI().startsWith("/api");

        if (isApiRequest) {
            // API: 바로 403 JSON
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    new ObjectMapper().writeValueAsString(CustomErrorResponse.of(ErrorCode.AUTH_FORBIDDEN))
            );
            return;
        }

        // 뷰: 에러 페이지로만 포워드/리다이렉트
        response.sendRedirect("/error-page/403");
    }

}
