package com.ssginc.showpingrefactoring.domain.member.service;

import com.ssginc.showpingrefactoring.domain.member.dto.request.AdminLoginRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.response.LoginResponseDto;

public interface AdminService {
    LoginResponseDto login(AdminLoginRequestDto request);
}
