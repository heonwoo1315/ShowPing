package com.ssginc.showpingrefactoring.domain.member.repository;

import com.ssginc.showpingrefactoring.domain.member.entity.MemberMfa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberMfaRepository extends JpaRepository<MemberMfa, Long> {
    Optional<MemberMfa> findByMemberNo(Long memberNo);
    boolean existsByMemberNo(Long memberNo);
}
