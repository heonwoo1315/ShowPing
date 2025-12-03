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
import java.util.Optional;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException, ServletException {

        final String accept = Optional.ofNullable(request.getHeader("Accept")).orElse("");
        final String xrw = Optional.ofNullable(request.getHeader("X-Requested-With")).orElse("");
        final String fetchMode = Optional.ofNullable(request.getHeader("Sec-Fetch-Mode")).orElse("");
        final String fetchDest = Optional.ofNullable(request.getHeader("Sec-Fetch-Dest")).orElse("");

        final boolean ajax = "XMLHttpRequest".equalsIgnoreCase(xrw);
        final boolean wantsHtml = accept.contains("text/html");
        final boolean isNavigate = "navigate".equalsIgnoreCase(fetchMode) || "document".equalsIgnoreCase(fetchDest);

        // 📌 규칙
        // 1) 주소창/링크로 들어온 "문서 네비게이션" + HTML 선호 => 403 HTML 페이지로 이동 (API 경로라도 예외)
        if ((isNavigate && wantsHtml && !ajax)) {
            response.sendRedirect("/error-page/403");
            return;
        }

        // 2) 그 외(XHR/Fetch, 프로그램틱 호출) => JSON 403
        //    - Accept: application/json
        //    - X-Requested-With: XMLHttpRequest
        //    - 또는 실제 API 호출 흐름
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                new ObjectMapper().writeValueAsString(CustomErrorResponse.of(ErrorCode.AUTH_FORBIDDEN))
        );
    }
}
