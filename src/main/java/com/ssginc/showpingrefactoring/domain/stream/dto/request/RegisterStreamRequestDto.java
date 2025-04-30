package com.ssginc.showpingrefactoring.domain.stream.dto.request;

import lombok.Getter;

@Getter
public class RegisterStreamRequestDto {

    private Long streamNo;

    private String streamTitle;

    private String streamDescription;

    private Long productNo;

    private Integer productSale;

}
