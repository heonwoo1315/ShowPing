package com.ssginc.showpingrefactoring.member.repository;

import com.ssginc.showpingrefactoring.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface MemberRepository extends JpaRepository<Member, Long> {

    // 🔹 memberId로 회원 조회 (로그인용)
    Optional<Member> findByMemberId(String memberId);

    // 🔹 이메일로 회원 조회 (선택사항)
    Optional<Member> findByMemberEmail(String email);
}
