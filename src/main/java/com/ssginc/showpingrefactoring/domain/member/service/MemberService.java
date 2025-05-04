package com.ssginc.showpingrefactoring.domain.member.service;


import com.ssginc.showpingrefactoring.domain.member.dto.request.SignupRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.request.UpdateMemberRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.object.MemberDto;

public interface MemberService {
    void signup(SignupRequestDto request);

    MemberDto getMemberInfo(String memberId);

    void updateMember(String memberId, UpdateMemberRequestDto request);

    void deleteMember(String memberId);
}
