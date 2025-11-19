package com.ssginc.showpingrefactoring.domain.member.service.implement;

import com.ssginc.showpingrefactoring.common.jwt.JwtTokenProvider;
import com.ssginc.showpingrefactoring.domain.member.service.RedisTokenService;
import com.ssginc.showpingrefactoring.domain.member.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Primary
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenService redisTokenService;

    @Override
    public void rotateRefresh(String memberId) {
        String oldRt = redisTokenService.getRefreshToken(memberId);
        String newRt = jwtTokenProvider.generateRefreshToken(memberId);
        if (oldRt == null) {
            redisTokenService.saveRefreshToken(memberId, newRt);
        } else {
            boolean ok = redisTokenService.rotateRefreshToken(memberId, oldRt, newRt);
            if (!ok) redisTokenService.saveRefreshToken(memberId, newRt);
        }
    }

    @Override
    public Map<String, Object> issueMfaTokens(Object principal, Map<String, Object> extraClaims) {
        String memberId;
        String role = null;

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            memberId = ud.getUsername();
            for (GrantedAuthority a : ud.getAuthorities()) {
                if (a.getAuthority().startsWith("ROLE_")) { role = a.getAuthority(); break; }
            }
        } else if (principal instanceof Authentication auth) {
            memberId = auth.getName();
            for (GrantedAuthority a : auth.getAuthorities()) {
                if (a.getAuthority().startsWith("ROLE_")) { role = a.getAuthority(); break; }
            }
        } else {
            memberId = String.valueOf(principal);
        }

        // --- RT2를 가져와서 RT3로 교체하는 로직 시작 ---
        String oldRt = redisTokenService.getRefreshToken(memberId); // RT2 가져옴

        Map<String,Object> claims = new HashMap<>();
        if (extraClaims != null) claims.putAll(extraClaims);

        String at = jwtTokenProvider.generateAccessToken(memberId, role, claims);
        String newRt = jwtTokenProvider.generateRefreshToken(memberId); // RT3 생성

        // rotateRefreshToken 로직 사용: RT2 인덱스 정리 후 RT3 갱신
        if (oldRt == null) {
            redisTokenService.saveRefreshToken(memberId, newRt);
        } else {
            boolean ok = redisTokenService.rotateRefreshToken(memberId, oldRt, newRt);
            if (!ok) redisTokenService.saveRefreshToken(memberId, newRt);
        }
        // --- RT 교체 로직 끝 ---

        Map<String,Object> out = new HashMap<>();
        out.put("accessToken", at);
        out.put("refreshToken", newRt); // newRt(RT3) 반환
        return out;
    }

}
