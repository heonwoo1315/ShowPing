package com.ssginc.showpingrefactoring.domain.member.service;

import com.ssginc.showpingrefactoring.domain.member.dto.AdminLoginRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.LoginResponseDto;

public interface AdminService {
    LoginResponseDto login(AdminLoginRequestDto request);
}
