package com.ssginc.showpingrefactoring.member.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // 예외 메시지에도 에러 메시지를 넣어주자
        this.errorCode = errorCode;
    }
}
