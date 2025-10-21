package com.ssginc.showpingrefactoring.common.util;

import com.ssginc.showpingrefactoring.common.jwt.AuthUser;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class MfaGuard {

    // MFA 유효 시간(초) — yml로 뺄 수도 있음
    private static final long MFA_MAX_AGE_SECONDS = 900L; // 15분

    public AuthorizationDecision decision(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean hasMfa = auth.getAuthorities().stream()
                .anyMatch(a -> "MFA".equals(a.getAuthority()));

        if (!isAdmin || !hasMfa) return new AuthorizationDecision(false);

        Object p = auth.getPrincipal();
        if (p instanceof AuthUser au) {
            Long ts = au.getMfaTs();
            if (ts == null) return new AuthorizationDecision(false);
            long age = Instant.now().getEpochSecond() - ts;
            return new AuthorizationDecision(age <= MFA_MAX_AGE_SECONDS);
        }
        return new AuthorizationDecision(false);
    }
}
