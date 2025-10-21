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
    public void rotateRefresh(Long memberNo) {
        String memberId = String.valueOf(memberNo);
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

        Map<String,Object> claims = new HashMap<>();
        if (extraClaims != null) claims.putAll(extraClaims);

        String at = jwtTokenProvider.generateAccessToken(memberId, role, claims);
        String rt = jwtTokenProvider.generateRefreshToken(memberId);
        redisTokenService.saveRefreshToken(memberId, rt);

        Map<String,Object> out = new HashMap<>();
        out.put("accessToken", at);
        out.put("refreshToken", rt);
        return out;
    }
}
