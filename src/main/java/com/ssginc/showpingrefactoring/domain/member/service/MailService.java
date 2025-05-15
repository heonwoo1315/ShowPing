package com.ssginc.showpingrefactoring.domain.member.service;

public interface MailService {

    String sendVerificationCode(String email);
    boolean verifyCode(String email, String inputCode);
    void send(String to,String subject,String text);


//    String sendSignupVerificationCode(String email);
//    boolean verifySignupCode(String email, String inputCode);
//    void sendTotpRegistrationMail(String email, String subject, String body);
}
