package com.ssginc.showpingrefactoring.domain.member.service.implement;


import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.domain.member.entity.MemberRole;
import com.ssginc.showpingrefactoring.domain.member.dto.request.SignupRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.request.UpdateMemberRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.object.MemberDto;
import com.ssginc.showpingrefactoring.common.exception.CustomException;
import com.ssginc.showpingrefactoring.common.exception.ErrorCode;
import com.ssginc.showpingrefactoring.domain.member.repository.MemberRepository;
import com.ssginc.showpingrefactoring.domain.member.service.MemberService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.beans.Transient;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, Boolean> verifiedEmailStorage = new HashMap<>();

    @Override
    public Member findMemberById(String memberId) {
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + memberId));
    }

    @Transactional
    @Override
    public void signup(SignupRequestDto request) {
        if (!verifiedEmailStorage.containsKey(request.getMemberEmail()) || !verifiedEmailStorage.get(request.getMemberEmail())) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        if (memberRepository.findByMemberId(request.getMemberId()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_MEMBER_ID);
        }

        if (memberRepository.findByMemberEmail(request.getMemberEmail()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }

        Member member = Member.builder()
                .memberName(request.getMemberName())
                .memberId(request.getMemberId())
                .memberEmail(request.getMemberEmail())
                .memberPassword(passwordEncoder.encode(request.getMemberPassword()))
                .memberAddress(request.getMemberAddress())
                .memberPhone(request.getMemberPhone())
                .memberRole(MemberRole.ROLE_USER) // 기본 USER
                .streamKey(UUID.randomUUID().toString())
                .memberPoint(0L)
                .build();

        memberRepository.save(member);

        // 회원가입 완료 후 인증 성공 기록 삭제 (optional)
        verifiedEmailStorage.remove(request.getMemberEmail());
    }

    @Override
    public boolean isDuplicateId(String memberId) {
        return memberRepository.existsByMemberId(memberId);
    }

    @Override
    public boolean isDuplicateEmail(String memberEmail) {
        return memberRepository.existsByMemberEmail(memberEmail);
    }

    @Override
    public MemberDto getMemberInfo(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return new MemberDto(
                member.getMemberNo(),
                member.getMemberId(),
                member.getMemberName(),
                member.getMemberEmail(),
                member.getMemberPhone(),
                member.getMemberAddress()
        );
    }

    @Override
    public void updateMember(String memberId, UpdateMemberRequestDto request) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (request.getMemberName() != null) {
            member.setMemberName(request.getMemberName());
        }
        if (request.getPassword() != null) {
            member.setMemberPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getAddress() != null) {
            member.setMemberAddress(request.getAddress());
        }

        memberRepository.save(member);
    }

    @Override
    public void deleteMember(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        memberRepository.delete(member);
    }
}
