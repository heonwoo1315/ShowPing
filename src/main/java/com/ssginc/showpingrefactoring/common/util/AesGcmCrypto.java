package com.ssginc.showpingrefactoring.common.util;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AesGcmCrypto {
    private static final int TAG_BITS = 128;
    private static final int IV_LEN = 12;

    private final byte[] key;
    private final SecureRandom rnd = new SecureRandom();

    public AesGcmCrypto(@Value("${crypto.aesGcmKeyBase64}") String base64Key) {
        this.key = Base64.getDecoder().decode(base64Key);
    }

    @PostConstruct
    void check() {
        if (key == null || key.length != 32) {
            throw new IllegalStateException("AES_GCM_KEY_B64 must be 32 bytes (base64).");
        }
    }

    public byte[] encrypt(byte[] plain) {
        try {
            byte[] iv = new byte[IV_LEN]; rnd.nextBytes(iv);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
            byte[] ct = c.doFinal(plain);
            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);
            return out;
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    public byte[] decrypt(byte[] enc) {
        try {
            byte[] iv = new byte[IV_LEN];
            System.arraycopy(enc, 0, iv, 0, IV_LEN);
            byte[] ct = new byte[enc.length - IV_LEN];
            System.arraycopy(enc, IV_LEN, ct, 0, ct.length);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
            return c.doFinal(ct);
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
