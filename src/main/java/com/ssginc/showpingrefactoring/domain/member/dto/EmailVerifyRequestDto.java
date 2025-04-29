package com.ssginc.showpingrefactoring.domain.member.dto;

import lombok.Getter;

@Getter
public class EmailVerifyRequestDto {
    private String email;
    private String code;
}
