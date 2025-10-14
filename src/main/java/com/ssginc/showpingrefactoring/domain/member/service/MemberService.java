package com.ssginc.showpingrefactoring.domain.member.service;


import com.ssginc.showpingrefactoring.domain.member.dto.request.SignupRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.request.UpdateMemberRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.object.MemberDto;
import com.ssginc.showpingrefactoring.domain.member.entity.Member;

public interface MemberService {
//    void signup(SignupRequestDto request);

    Member registerMember(MemberDto memberDto) throws Exception;

    MemberDto getMemberInfo(String memberId);

    void updateMember(String memberId, UpdateMemberRequestDto request);

    void deleteMember(String memberId);

    Member findMemberById(String memberId);

    boolean isDuplicateId(String memberId);

    boolean isDuplicateEmail(String memberEmail);

    boolean isDuplicatePhone(String memberPhone);

    Member findMember(String memberId, String password);
}
