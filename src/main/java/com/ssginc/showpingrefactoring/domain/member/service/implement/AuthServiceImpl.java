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
        String memberId = request.getMemberId();
        String rawPassword = request.getPassword();

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(rawPassword, member.getMemberPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String role = member.getMemberRole().name();

        String accessToken = jwtTokenProvider.generateAccessToken(memberId, role);
        String refreshToken = jwtTokenProvider.generateRefreshToken(memberId);
        redisTokenService.saveRefreshToken(memberId, refreshToken);

        // ✅ 관리자/사용자 구분 없이 바로 로그인 성공
        return new LoginResponseDto(
                "LOGIN_SUCCESS",
                accessToken,
                refreshToken
        );
    }

//    @Override
//    public TokenResponseDto reissue(ReissueRequestDto request) {
//        String refreshToken = request.getRefreshToken();
//        String memberId = jwtTokenProvider.getUsername(refreshToken);
//
//        if (!redisTokenService.validateRefreshToken(memberId, refreshToken)) {
//            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
//        }
//
//        String newAccessToken = jwtTokenProvider.generateAccessToken(memberId, jwtTokenProvider.getRole(refreshToken));
//
//        return new TokenResponseDto(newAccessToken);
//    }

    @Override
    public void logout(String memberId) {
        redisTokenService.deleteRefreshToken(memberId);
    }

    @Override
    public String getMemberIdFromToken(String token) {
        return jwtTokenProvider.getUsername(token);
    }
}
