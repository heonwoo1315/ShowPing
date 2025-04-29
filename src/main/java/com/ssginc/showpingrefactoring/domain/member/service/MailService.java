package com.ssginc.showpingrefactoring.domain.member.service;

public interface MailService {
    String sendSignupVerificationCode(String email);
    boolean verifySignupCode(String email, String inputCode);
    void sendTotpRegistrationMail(String email, String subject, String body);
}
