package com.ssginc.showpingrefactoring.common.jwt;

import com.ssginc.showpingrefactoring.common.util.MfaPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class AuthUser implements UserDetails, MfaPrincipal {
    private final String memberId; // 로그인 ID (ex. "ShowPing_Admin")
    private final Long memberNo;   // 숫자 멤버번호 (JWT mid)
    private final Collection<? extends GrantedAuthority> authorities;
    private final Long mfaTs;
    private final String deviceId;

    public AuthUser(String memberId,
                    Long memberNo,
                    Collection<? extends GrantedAuthority> authorities,
                    Long mfaTs,
                    String deviceId) {
        this.memberId = memberId;
        this.memberNo = memberNo;
        this.authorities = authorities;
        this.mfaTs = mfaTs;
        this.deviceId = deviceId;
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities(){ return authorities; }
    @Override public String getPassword(){ return ""; }
    @Override public String getUsername(){ return memberId; }
    @Override public boolean isAccountNonExpired(){ return true; }
    @Override public boolean isAccountNonLocked(){ return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public boolean isEnabled(){ return true; }

    // 더 이상 memberId를 Long으로 변환하지 않음
    @Override public Long getMemberNo(){ return memberNo; }
    @Override public Long getMfaTs(){ return mfaTs; }
    @Override public String getDeviceId(){ return deviceId; }
}
