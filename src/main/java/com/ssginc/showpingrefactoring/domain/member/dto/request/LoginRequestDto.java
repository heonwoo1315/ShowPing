package com.ssginc.showpingrefactoring.domain.member.dto.request;

import lombok.Getter;

@Getter
public class LoginRequestDto {
    private String memberId;
    private String password;
}
