package com.ssginc.showpingrefactoring.common.util;

import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.domain.member.entity.MemberRole;
import com.ssginc.showpingrefactoring.domain.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
public class AdminBootstrap {

    @Value("${admin.password}")
    private String adminPassword;

    @Bean
    CommandLineRunner createAdmin(MemberRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.findByMemberId("ShowPing_Admin").isEmpty()) {
                Member admin = Member.builder()
                        .memberId("ShowPing_Admin")
                        .memberName("관리자")
                        .memberPassword(encoder.encode(adminPassword))
                        .memberEmail("admin@showping.local")
                        .memberRole(MemberRole.ROLE_ADMIN)
                        .memberAddress("Seoul")
                        .build();
                repo.save(admin);
                System.out.println("✅ Admin user created.");
            } else {
                System.out.println("ℹ️ Admin already exists, skipped.");
            }
        };
    }
}
