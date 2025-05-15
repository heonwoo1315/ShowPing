package com.ssginc.showpingrefactoring.domain.member.controller;

import com.ssginc.showpingrefactoring.domain.member.dto.request.AdminLoginRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.response.LoginResponseDto;
import com.ssginc.showpingrefactoring.domain.member.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "관리자 로그인", description = "관리자 ID와 비밀번호로 로그인하여 토큰을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody AdminLoginRequestDto request) {
        LoginResponseDto response = adminService.login(request);
        return ResponseEntity.ok(response);
    }
}
