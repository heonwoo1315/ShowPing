package com.ssginc.showpingrefactoring.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {

    // HTTP status code
    private final HttpStatus status;

    // Custom response code
    private final String code;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // 예외 메시지에도 에러 메시지를 넣어주자
        this.status = errorCode.getStatus();
        this.code = errorCode.getCode();
    }
}
