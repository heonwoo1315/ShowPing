package com.ssginc.showpingrefactoring.domain.member.service.implement;

import com.ssginc.showpingrefactoring.domain.member.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final Map<String, String> emailCodeStorage = new HashMap<>(); // 이메일 인증 코드 저장소

    // 🔹 회원가입용 인증 코드 생성 및 이메일 전송
    @Override
    public String sendSignupVerificationCode(String email) {
        String code = generateCode();
        emailCodeStorage.put(email, code);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("쇼핑몰 회원가입 인증 코드");
            helper.setText("<h3>회원가입 인증 코드: <strong>" + code + "</strong></h3>", true);

            mailSender.send(message);
            return "이메일 전송 완료!";
        } catch (MessagingException e) {
            e.printStackTrace();
            return "이메일 전송 실패!";
        }
    }

    // 🔹 회원가입용 인증 코드 검증
    @Override
    public boolean verifySignupCode(String email, String inputCode) {
        return emailCodeStorage.containsKey(email) && emailCodeStorage.get(email).equals(inputCode);
    }

    // 🔹 TOTP 등록 이메일 전송 (일반 텍스트)
    @Override
    public void sendTotpRegistrationMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    // 🔹 6자리 인증 코드 생성
    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
