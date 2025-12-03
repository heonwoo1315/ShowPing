package com.ssginc.showpingrefactoring.domain.member.service;

import com.ssginc.showpingrefactoring.domain.member.dto.request.LoginRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.response.LoginResponseDto;
import com.ssginc.showpingrefactoring.domain.member.dto.request.ReissueRequestDto;

import java.util.Map;

public interface AuthService {
    Map<String, String> login(LoginRequestDto request);

    void deleteAllSessions(String memberId);

    String[] reissue(String refreshToken);

}
