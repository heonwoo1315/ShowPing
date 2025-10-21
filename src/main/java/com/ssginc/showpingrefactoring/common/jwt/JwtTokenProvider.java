package com.ssginc.showpingrefactoring.common.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;


@Component
public class JwtTokenProvider {
    public static final String CLAIM_MEMBER_NO = "memberNo";
    public static final String CLAIM_ROLES     = "roles";
    public static final String CLAIM_MFA       = "mfa";
    public static final String MFA_PENDING     = "PENDING";
    public static final String MFA_VERIFIED    = "VERIFIED";
    public static final String ROLE_PRE_AUTH   = "ROLE_PRE_AUTH";
    public static final String ROLE_ADMIN      = "ROLE_ADMIN";
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


    // 기존(호환용) - memberNo 없이 발급
    public String generateAccessToken(String memberId, String role) {
        return createToken(memberId, role, null, accessTokenExpiration);
    }

    // ✅ 권장: memberNo 포함해서 발급 (mid 클레임에 담음)
    public String generateAccessToken(String memberId, Long memberNo, String role) {
        Map<String, Object> extra = new HashMap<>();
        if (memberNo != null) extra.put("mid", memberNo);
        return createToken(memberId, role, extra, accessTokenExpiration);
    }

    // 필요 시 추가 클레임으로 발급
    public String generateAccessToken(String memberId, String role, Map<String, Object> extraClaims) {
        return createToken(memberId, role, extraClaims, accessTokenExpiration);
    }

    // Refresh Token (role 미포함)
    public String generateRefreshToken(String memberId) {
        return createToken(memberId, null, null, refreshTokenExpiration);
    }

    private String createToken(String memberId, String role, Map<String, Object> extraClaims, long expirationTime) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(memberId) // ex) "ShowPing_Admin"
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256);

        if (role != null) builder.claim("role", role);
        if (extraClaims != null) extraClaims.forEach(builder::claim);

        return builder.compact();
    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        Claims claims = parseClaims(token);

        String username = claims.getSubject();
        String role = normalizeRole(Objects.toString(claims.get("role"), null));

        // ✅ mid 읽기 (Number/String 모두 수용)
        Long memberNo = null;
        Object mid = claims.get("mid");
        if (mid instanceof Number n) memberNo = n.longValue();
        else if (mid instanceof String s && s.matches("\\d+")) memberNo = Long.valueOf(s);

        boolean mfa = Boolean.TRUE.equals(claims.get("mfa"));
        Long mfaTs = null;
        Object mts = claims.get("mfa_ts");
        if (mts instanceof Number n2) mfaTs = n2.longValue();

        String deviceId = Objects.toString(claims.get("device_id"), null);

        var auths = new ArrayList<SimpleGrantedAuthority>();
        if (role != null) auths.add(new SimpleGrantedAuthority(role));
        if (mfa)         auths.add(new SimpleGrantedAuthority("MFA"));

        var principal = new AuthUser(username, memberNo, auths, mfaTs, deviceId);
        return new UsernamePasswordAuthenticationToken(principal, token, auths);
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRole(String token) {
        Object role = parseClaims(token).get("role");
        return role != null ? role.toString() : null;
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

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

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

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
            return e.getClaims().getSubject();
        }
    }
}
