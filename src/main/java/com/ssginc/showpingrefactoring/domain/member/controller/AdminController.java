package com.ssginc.showpingrefactoring.domain.member.controller;

import com.ssginc.showpingrefactoring.domain.member.dto.request.AdminLoginRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.response.LoginResponseDto;
import com.ssginc.showpingrefactoring.domain.member.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 관리자 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody AdminLoginRequestDto request) {
        LoginResponseDto response = adminService.login(request);
        return ResponseEntity.ok(response);
    }
}
