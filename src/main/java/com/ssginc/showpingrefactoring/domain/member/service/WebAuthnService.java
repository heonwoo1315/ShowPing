package com.ssginc.showpingrefactoring.domain.member.service;

import java.util.Map;
import java.util.UUID;

public interface WebAuthnService {
    Map<String,Object> assertionOptions(Long memberNo);
    UUID verifyAssertion(Long memberNo, byte[] rawId, byte[] authenticatorData, byte[] clientDataJSON, byte[] signature);
}
