package com.ssginc.showpingrefactoring.domain.member.service;

import com.ssginc.showpingrefactoring.domain.member.dto.request.LoginRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.response.LoginResponseDto;
import com.ssginc.showpingrefactoring.domain.member.dto.request.ReissueRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.response.TokenResponseDto;

public interface AuthService {
    LoginResponseDto login(LoginRequestDto request);
//    TokenResponseDto reissue(ReissueRequestDto request);
    void logout(String memberId);

    // 🔹 AccessToken에서 memberId 추출
    String getMemberIdFromToken(String token);
}
