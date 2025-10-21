package com.ssginc.showpingrefactoring.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

@Component
public class MfaPrincipalExtractor {

    public Optional<MfaPrincipal> extract(Authentication auth){
        if (auth == null || !auth.isAuthenticated()) return Optional.empty();
        Object p = auth.getPrincipal();
        if (p == null) return Optional.empty();

        if (p instanceof MfaPrincipal mp) return Optional.of(mp);

        // Jwt (spring-security-oauth2)
        try {
            Class<?> jwt = Class.forName("org.springframework.security.oauth2.jwt.Jwt");
            if (jwt.isInstance(p)) {
                Map<String,Object> claims = (Map<String,Object>) jwt.getMethod("getClaims").invoke(p);
                return Optional.of(mapClaims(claims, auth.getAuthorities()));
            }
        } catch (Exception ignore) {}

        if (p instanceof Map<?,?> map) {
            return Optional.of(mapClaims((Map<String,Object>) map, auth.getAuthorities()));
        }

        // 커스텀 Principal 리플렉션
        try {
            Method gm = p.getClass().getMethod("getMemberNo");
            Long memberNo = (Long) gm.invoke(p);

            Long mfaTs = null; String deviceId = null;
            try { mfaTs = (Long) p.getClass().getMethod("getMfaTs").invoke(p); } catch (NoSuchMethodException ignore) {}
            try { deviceId = (String) p.getClass().getMethod("getDeviceId").invoke(p); } catch (NoSuchMethodException ignore) {}

            final Long fM = memberNo; final Long fT = mfaTs; final String fD = deviceId;
            return Optional.of(new MfaPrincipal() {
                public Long getMemberNo(){ return fM; }
                public Long getMfaTs(){ return fT; }
                public String getDeviceId(){ return fD; }
                public Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities(){ return auth.getAuthorities(); }
            });
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    private MfaPrincipal mapClaims(Map<String,Object> claims, Collection<? extends org.springframework.security.core.GrantedAuthority> auths){
        Long memberNo = null;
        Object v = claims.get("memberNo");
        if (v instanceof Number n) memberNo = n.longValue();
        if (memberNo == null && claims.get("sub") instanceof String s && s.matches("\\d+")) memberNo = Long.parseLong(s);

        Long mfaTs = null; Object mts = claims.get("mfa_ts");
        if (mts instanceof Number n) mfaTs = n.longValue();

        String deviceId = null; Object did = claims.get("device_id");
        if (did instanceof String ds) deviceId = ds;

        final Long fM = memberNo; final Long fT = mfaTs; final String fD = deviceId;
        return new MfaPrincipal() {
            public Long getMemberNo(){ return fM; }
            public Long getMfaTs(){ return fT; }
            public String getDeviceId(){ return fD; }
            public Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities(){ return auths; }
        };
    }
}
