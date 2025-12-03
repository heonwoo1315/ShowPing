package com.ssginc.showpingrefactoring.common.util;

import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;

public interface MfaPrincipal {
    Long getMemberNo();
    Long getMfaTs();             // epoch seconds (nullable)
    String getDeviceId();        // UUID string (nullable)
    Collection<? extends GrantedAuthority> getAuthorities();
}
