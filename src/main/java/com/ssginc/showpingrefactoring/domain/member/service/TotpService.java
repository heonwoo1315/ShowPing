package com.ssginc.showpingrefactoring.domain.member.service;

public interface TotpService {
    String issueSecret(Long memberNo);
    void verify(Long memberNo, String code);
}
