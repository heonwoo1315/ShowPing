package com.ssginc.showpingrefactoring.domain.member.service.implement;

import com.ssginc.showpingrefactoring.common.util.AesGcmCrypto;
import com.ssginc.showpingrefactoring.domain.member.entity.MemberMfa;
import com.ssginc.showpingrefactoring.domain.member.repository.MemberMfaRepository;
import com.ssginc.showpingrefactoring.domain.member.service.TotpService;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class TotpServiceImpl implements TotpService {

    private final MemberMfaRepository repo;
    private final AesGcmCrypto crypto;
    private final DefaultCodeVerifier verifier;

    public TotpServiceImpl(MemberMfaRepository repo,
                           AesGcmCrypto crypto,
                           @Value("${mfa.allowedStepSkew:1}") int skew,
                           @Value("${mfa.periodSeconds:30}") int period) {
        this.repo = repo;
        this.crypto = crypto;
        var gen = new DefaultCodeGenerator();
        var v = new DefaultCodeVerifier(gen, new SystemTimeProvider());
        v.setTimePeriod(period);
        v.setAllowedTimePeriodDiscrepancy(skew);
        this.verifier = v;
    }

    @Override
    public String issueSecret(Long memberNo) {
        var m = repo.findById(memberNo).orElse(null);
        if (m != null && m.isEnabled() && m.getSecretEnc() != null) {
            return new String(crypto.decrypt(m.getSecretEnc()), StandardCharsets.US_ASCII);
        }
        SecretGenerator generator = new DefaultSecretGenerator();
        String secret = generator.generate(); // Base32
        byte[] enc = crypto.encrypt(secret.getBytes(StandardCharsets.US_ASCII));
        if (m == null) {
            m = new MemberMfa();
            m.setMemberNo(memberNo);     // <-- 공유 PK 제거했으므로 이것만 세팅하면 됨
        }
        m.setSecretEnc(enc);
        m.setEnrolledAt(Instant.now());
        repo.save(m);
        return secret;
    }

    @Override
    public void verify(Long memberNo, String code) {
        var m = repo.findById(memberNo).orElseThrow(() -> new IllegalArgumentException("MFA not enrolled"));
        var secret = new String(crypto.decrypt(m.getSecretEnc()), StandardCharsets.US_ASCII);
        if (!verifier.isValidCode(secret, code)) throw new IllegalArgumentException("Invalid TOTP");
        m.setLastVerifiedAt(Instant.now());
        repo.save(m);
    }


}
