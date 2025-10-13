package com.ssginc.showpingrefactoring.common.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access.expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh.expiration}") long refreshTokenExpiration
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // Access Token 생성 (role 포함)
    public String generateAccessToken(String memberId, String role) {
        return createToken(memberId, role, accessTokenExpiration);
    }

    // Refresh Token 생성 (role 미포함)
    public String generateRefreshToken(String memberId) {
        return createToken(memberId, null, refreshTokenExpiration);
    }

    private String createToken(String memberId, String role, long expirationTime) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(memberId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256);

        if (role != null) {
            builder.claim("role", role);
        }
        return builder.compact();
    }

    /** Authentication 생성 (role 없거나 형식 다른 경우 방어) */
    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        String username = getUsername(token);
        String role = normalizeRole(getRole(token)); // null/빈값/ROLE_ 접두어 처리
        UserDetails userDetails = new User(username, "",
                role == null ? List.of() : List.of(new SimpleGrantedAuthority(role)));
        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }

    /** 토큰에서 subject */
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /** 토큰에서 role (없으면 null) */
    public String getRole(String token) {
        Object role = parseClaims(token).get("role");
        return role != null ? role.toString() : null;
    }

    /** 토큰 유효성 검사 (만료면 false) */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // 만료: 필터에서 구분 처리하므로 여기선 false만 반환
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** (필요 시) 만료 여부만 알고 싶을 때 */
    public boolean isExpired(String token) {
        try {
            parseClaims(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** 내부: 클레임 파싱 */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** ROLE_ 접두어 통일 & 빈값 방어 */
    private String normalizeRole(String role) {
        if (role == null) return null;
        String r = role.trim();
        if (r.isEmpty()) return null;
        return r.startsWith("ROLE_") ? r : "ROLE_" + r;
    }

    public String getUsernameAllowExpired(String token) {
        try {
            return getUsername(token);
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject(); // 만료된 토큰에서도 subject 추출
        }
    }

}
