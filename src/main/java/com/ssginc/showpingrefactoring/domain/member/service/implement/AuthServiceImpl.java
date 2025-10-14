package com.ssginc.showpingrefactoring.domain.member.service.implement;

import com.ssginc.showpingrefactoring.domain.member.dto.request.LoginRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.request.ReissueRequestDto;
import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.common.exception.CustomException;
import com.ssginc.showpingrefactoring.common.exception.ErrorCode;
import com.ssginc.showpingrefactoring.common.jwt.JwtTokenProvider;
import com.ssginc.showpingrefactoring.domain.member.repository.MemberRepository;
import com.ssginc.showpingrefactoring.domain.member.service.AuthService;

import com.ssginc.showpingrefactoring.domain.member.service.RedisTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenService redisTokenService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Map<String, String> login(LoginRequestDto request) {
        String memberId = request.getMemberId();
        String rawPassword = request.getPassword();

        // 1. 사용자 인증 및 비밀번호 검증 (기존 로직 유지)
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(rawPassword, member.getMemberPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String role = member.getMemberRole().name();

        // 2. 토큰 생성 및 Redis 저장
        String accessToken = jwtTokenProvider.generateAccessToken(memberId, role);
        String refreshToken = jwtTokenProvider.generateRefreshToken(memberId);
        redisTokenService.saveRefreshToken(memberId, refreshToken);

        // 3. 토큰을 Map 형태로 반환 (Controller가 쿠키로 설정하고 DTO를 구성하도록)
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        tokens.put("status", "LOGIN_SUCCESS");

        return tokens;
    }

    @Override
    public String[] reissue(String refreshTokenFromCookie) {
        // 1) 쿠키의 RT 파싱 → memberId 확인
        String memberId;
        try {
            memberId = jwtTokenProvider.getUsernameAllowExpired(refreshTokenFromCookie);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 2) 저장된 RT와 일치 확인 (훔친/오래된 RT 방지)
        String stored = redisTokenService.getRefreshToken(memberId);
        if (stored == null || !stored.equals(refreshTokenFromCookie)) {
            // 불일치: 공격/오래된 RT → 강제 로그아웃
            redisTokenService.deleteRefreshToken(refreshTokenFromCookie);
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 3) 새 AT/RT 생성
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String role = member.getMemberRole().name();
        String newAT = jwtTokenProvider.generateAccessToken(memberId, role);
        String newRT = jwtTokenProvider.generateRefreshToken(memberId);

        // 4) 회전: old → new (인덱스 교체)
        boolean ok = redisTokenService.rotateRefreshToken(memberId, refreshTokenFromCookie, newRT);
        if (!ok) throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);

        return new String[]{newAT, newRT};
    }

    @Override
    public void logoutByRefreshToken(String refreshToken) {
        redisTokenService.deleteRefreshTokenByRt(refreshToken);
    }

    @Override
    public String getMemberIdFromToken(String token) {
        return jwtTokenProvider.getUsername(token);
    }
}
