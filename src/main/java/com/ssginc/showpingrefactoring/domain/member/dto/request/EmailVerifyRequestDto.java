package com.ssginc.showpingrefactoring.domain.member.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerifyRequestDto {
    private String email;
    private String emailCode;
}
