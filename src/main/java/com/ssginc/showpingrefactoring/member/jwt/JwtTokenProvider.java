package com.ssginc.showpingrefactoring.member.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Key;
import java.util.Date;
import java.util.List;

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
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // 🔹 Access Token 생성
    public String generateAccessToken(String memberId, String role) {
        return createToken(memberId, role, accessTokenExpiration);
    }

    // 🔹 Refresh Token 생성
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

    // 🔹 토큰에서 Authentication 객체 생성
    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        String username = getUsername(token);
        String role = getRole(token);

        UserDetails userDetails = new User(username, "", List.of(new SimpleGrantedAuthority(role)));
        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }

    // 🔹 토큰에서 사용자명 추출
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // 🔹 토큰에서 역할 추출
    public String getRole(String token) {
        Object role = parseClaims(token).get("role");
        return role != null ? role.toString() : null;
    }

    // 🔹 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("JWT 만료: " + e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("JWT 검증 실패: " + e.getMessage());
        }
        return false;
    }

    // 🔹 Claims 파싱
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
