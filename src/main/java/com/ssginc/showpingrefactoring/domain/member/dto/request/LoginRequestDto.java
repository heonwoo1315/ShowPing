package com.ssginc.showpingrefactoring.domain.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequestDto {
    private String memberId;
    private String password;
}
