package com.ssginc.showpingrefactoring.domain.stream.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestCookieController {
    @GetMapping("/api/debug/set-cookie")
    public ResponseEntity<Void> setCookie(HttpServletRequest req) {
        boolean isLocal = "localhost".equalsIgnoreCase(req.getServerName()) || "127.0.0.1".equals(req.getServerName());
        ResponseCookie c = ResponseCookie.from("accessToken", "test")
                .httpOnly(true).secure(!isLocal).sameSite("Lax").path("/").maxAge(600).build();
        return ResponseEntity.ok().header("Set-Cookie", c.toString()).build();
    }
}
