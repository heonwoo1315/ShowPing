package com.ssginc.showpingrefactoring.domain.member.service;

import java.util.Map;

public interface TokenService {
    void rotateRefresh(Long memberNo);
    Map<String,Object> issueMfaTokens(Object principal, Map<String,Object> extraClaims);
}
