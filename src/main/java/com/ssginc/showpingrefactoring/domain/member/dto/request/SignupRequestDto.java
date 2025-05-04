package com.ssginc.showpingrefactoring.domain.member.dto.request;

import lombok.Getter;

@Getter
public class SignupRequestDto {
    private String memberId;
    private String memberName;
    private String password;
    private String email;
    private String phone;
    private String address;
}
