package com.ssginc.showpingrefactoring.domain.member.service;

import com.ssginc.showpingrefactoring.domain.member.dto.request.LoginRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.response.LoginResponseDto;
import com.ssginc.showpingrefactoring.domain.member.dto.request.ReissueRequestDto;

import java.util.Map;

public interface AuthService {
    Map<String, String> login(LoginRequestDto request);
//    TokenResponseDto reissue(ReissueRequestDto request);
    void logoutByRefreshToken(String refreshToken);

    String[] reissue(String refreshToken);

}
