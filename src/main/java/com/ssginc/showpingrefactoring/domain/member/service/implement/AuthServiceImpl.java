package com.ssginc.showpingrefactoring.domain.member.service.implement;


import com.ssginc.showpingrefactoring.domain.member.dto.request.LoginRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.response.LoginResponseDto;
import com.ssginc.showpingrefactoring.domain.member.dto.request.ReissueRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.response.TokenResponseDto;
import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.common.exception.CustomException;
import com.ssginc.showpingrefactoring.common.exception.ErrorCode;
import com.ssginc.showpingrefactoring.common.jwt.JwtTokenProvider;
import com.ssginc.showpingrefactoring.common.jwt.RedisTokenService;
import com.ssginc.showpingrefactoring.domain.member.repository.MemberRepository;
import com.ssginc.showpingrefactoring.domain.member.service.AuthService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenService redisTokenService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        Member member = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), member.getMemberPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(member.getMemberId(), member.getMemberRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(member.getMemberId());

        redisTokenService.saveRefreshToken(member.getMemberId(), refreshToken);

        return new LoginResponseDto(accessToken, refreshToken);
    }

    @Override
    public TokenResponseDto reissue(ReissueRequestDto request) {
        String refreshToken = request.getRefreshToken();
        String memberId = jwtTokenProvider.getUsername(refreshToken);

        if (!redisTokenService.validateRefreshToken(memberId, refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(memberId, jwtTokenProvider.getRole(refreshToken));

        return new TokenResponseDto(newAccessToken);
    }

    @Override
    public void logout(String memberId) {
        redisTokenService.deleteRefreshToken(memberId);
    }

    @Override
    public String getMemberIdFromToken(String token) {
        return jwtTokenProvider.getUsername(token);
    }
}
