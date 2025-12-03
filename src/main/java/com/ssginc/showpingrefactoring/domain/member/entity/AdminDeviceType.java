package com.ssginc.showpingrefactoring.domain.member.entity;

public enum AdminDeviceType {
    WEB_AUTHN,   // 플랫폼 인증기(WebAuthn)
    MTLS,        // mTLS 클라이언트 인증서
    ATTEST       // 모바일 무결성(Play Integrity / App Attest 등)
}
