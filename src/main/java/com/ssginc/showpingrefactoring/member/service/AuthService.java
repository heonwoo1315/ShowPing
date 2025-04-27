package com.ssginc.showpingrefactoring.member.service;

import com.ssginc.showpingrefactoring.member.dto.LoginRequestDto;
import com.ssginc.showpingrefactoring.member.dto.LoginResponseDto;
import com.ssginc.showpingrefactoring.member.dto.ReissueRequestDto;
import com.ssginc.showpingrefactoring.member.dto.TokenResponseDto;

public interface AuthService {
    LoginResponseDto login(LoginRequestDto request);
    TokenResponseDto reissue(ReissueRequestDto request);
    void logout(String memberId);

    // 🔹 AccessToken에서 memberId 추출
    String getMemberIdFromToken(String token);
}
