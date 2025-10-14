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
        // 인증/인가 예외는 Security가 처리해야 하므로 if문을 써서 가로채지 않게 한다.
        // 모든 예외를 이 클래스가 처리하고 있기때문에 401/403흐름까지 여기서 가로채서 제대로된 401에러를 reissue 엔드포인트가 받아들이지 못해
        // 재발급이 제대로 되지 않고 있었다.
        if (e instanceof org.springframework.security.core.AuthenticationException
        || e instanceof org.springframework.security.access.AccessDeniedException
        || e instanceof org.springframework.security.authentication.InsufficientAuthenticationException) {
            throw (RuntimeException) e; // 그대로 전파 -> ExceptionTranslationFilter -> EntryPoint/AccessDeniedHandler
        }
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
