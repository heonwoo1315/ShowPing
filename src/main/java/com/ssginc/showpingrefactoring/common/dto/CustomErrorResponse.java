package com.ssginc.showpingrefactoring.common.dto;

import com.ssginc.showpingrefactoring.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class CustomErrorResponse {

    private final String code;

    private final String message;

    public static CustomErrorResponse of(String code, String message) {
        return new CustomErrorResponse(code, message);
    }

    public static CustomErrorResponse of(ErrorCode errorCode) {
        return new CustomErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }

    public CustomErrorResponse(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

}
