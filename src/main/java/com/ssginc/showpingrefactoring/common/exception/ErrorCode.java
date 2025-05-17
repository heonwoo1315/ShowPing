package com.ssginc.showpingrefactoring.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    //[회원 관련 에러 코드 추가]
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "회원을 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "M002", "비밀번호가 일치하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "M003", "RefreshToken이 유효하지 않습니다."),

    DUPLICATED_MEMBER_ID(HttpStatus.CONFLICT, "M004", "이미 사용 중인 회원 ID입니다."),
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "M005", "이미 사용 중인 이메일입니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "M006", "관리자 권한이 필요합니다."),
    TOTP_NOT_REGISTERED(HttpStatus.BAD_REQUEST, "M007", "TOTP 등록이 완료되지 않은 사용자입니다."),
    INVALID_TOTP_CODE(HttpStatus.UNAUTHORIZED, "M008", "잘못된 OTP 인증 코드입니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "M009", "이메일 인증이 완료되지 않았습니다."),

    //(추후 추가 가능: 이미 존재하는 이메일/아이디 등)
    ;

    // HTTP status code
    private final HttpStatus status;

    // Custom response code
    private final String code;

    // Custome response message
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
