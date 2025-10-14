package com.ssginc.showpingrefactoring.domain.member.dto.request;

import lombok.Getter;

@Getter
public class SignupRequestDto {
    private Long memberNo;
    private String memberId;
    private String memberName;
    private String memberEmail;
    private String memberPassword;
    private String memberAddress;
    private String memberPhone;
}
