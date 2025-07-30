package com.ssginc.showpingrefactoring.common.handler;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.ssginc.showpingrefactoring.common.dto.CustomErrorResponse;
import com.ssginc.showpingrefactoring.common.exception.CustomException;
import com.ssginc.showpingrefactoring.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CustomErrorResponse> handleCustomException(CustomException e, HttpServletRequest request) {
        log.error("Custom Response Code : {}, URL : {}, Custom Response Msg : {}", e.getCode(), request.getRequestURI(), e.getMessage(), e);

        return ResponseEntity
                .status(e.getStatus())
                .body(CustomErrorResponse.of(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorResponse> handleException(Exception e, HttpServletRequest request) {
        log.error("URL : {}, Response Msg : {}", request.getRequestURI(), e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomErrorResponse.of("INTERNAL_SERVER_ERROR", "알 수 없는 서버 오류가 발생했습니다."));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<CustomErrorResponse> handleBindException() {
        ErrorCode e = ErrorCode.INVALID_METHOD_ARGUMENT;

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CustomErrorResponse.of(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(AmazonS3Exception.class)
    public ResponseEntity<CustomErrorResponse> handleAmazonS3Exception() {
        ErrorCode e = ErrorCode.RESOURCE_NOT_FOUND;

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(CustomErrorResponse.of(e.getCode(), e.getMessage()));
    }

}
