package com.ssginc.showpingrefactoring.domain.member.service.implement;


import com.ssginc.showpingrefactoring.common.exception.CustomException;
import com.ssginc.showpingrefactoring.common.exception.ErrorCode;
import com.ssginc.showpingrefactoring.common.jwt.JwtTokenProvider;
import com.ssginc.showpingrefactoring.common.jwt.RedisTokenService;
import com.ssginc.showpingrefactoring.domain.member.dto.request.AdminLoginRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.response.LoginResponseDto;
import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.domain.member.entity.MemberRole;
import com.ssginc.showpingrefactoring.domain.member.repository.MemberRepository;
import com.ssginc.showpingrefactoring.domain.member.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenService redisTokenService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponseDto login(AdminLoginRequestDto request) {
        Member admin = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), admin.getMemberPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        if (!admin.getMemberRole().equals(MemberRole.ROLE_ADMIN)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(admin.getMemberId(), admin.getMemberRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(admin.getMemberId());

        redisTokenService.saveRefreshToken(admin.getMemberId(), refreshToken);

        log.info("관리자 로그인 성공: {}", admin.getMemberId());

        return new LoginResponseDto(accessToken, refreshToken);
    }
}
