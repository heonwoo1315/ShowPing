package com.ssginc.showpingrefactoring.common.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CsrfController {
    @GetMapping("/api/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        // 이 호출 한 번으로 XSRF-TOKEN 쿠키가 응답에 실리고, token도 반환됨
        return Map.of("token", token.getToken());
    }
}
