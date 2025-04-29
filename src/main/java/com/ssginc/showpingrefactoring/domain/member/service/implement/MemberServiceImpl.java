package com.ssginc.showpingrefactoring.domain.member.service.implement;


import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.domain.member.entity.MemberRole;
import com.ssginc.showpingrefactoring.domain.member.dto.SignupRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.UpdateMemberRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.MemberDto;
import com.ssginc.showpingrefactoring.common.exception.CustomException;
import com.ssginc.showpingrefactoring.common.exception.ErrorCode;
import com.ssginc.showpingrefactoring.domain.member.repository.MemberRepository;
import com.ssginc.showpingrefactoring.domain.member.service.MailService;
import com.ssginc.showpingrefactoring.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, Boolean> verifiedEmailStorage = new HashMap<>();

    @Override
    public void signup(SignupRequestDto request) {
        if (!verifiedEmailStorage.containsKey(request.getEmail()) || !verifiedEmailStorage.get(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        if (memberRepository.findByMemberId(request.getMemberId()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_MEMBER_ID);
        }

        if (memberRepository.findByMemberEmail(request.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }

        Member member = Member.builder()
                .memberId(request.getMemberId())
                .memberName(request.getMemberName())
                .memberPassword(passwordEncoder.encode(request.getPassword()))
                .memberEmail(request.getEmail())
                .memberPhone(request.getPhone())
                .memberRole(MemberRole.ROLE_USER) // 기본 USER
                .memberAddress(request.getAddress())
                .build();

        memberRepository.save(member);

        // 회원가입 완료 후 인증 성공 기록 삭제 (optional)
        verifiedEmailStorage.remove(request.getEmail());
    }

    @Override
    public MemberDto getMemberInfo(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return new MemberDto(
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
