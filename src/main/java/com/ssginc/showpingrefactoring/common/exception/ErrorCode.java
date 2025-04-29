package com.ssginc.showpingrefactoring.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    //[회원 관련 에러 코드 추가]
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "RefreshToken이 유효하지 않습니다."),

    DUPLICATED_MEMBER_ID(HttpStatus.CONFLICT, "이미 사용 중인 회원 ID입니다."),
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다."),
    TOTP_NOT_REGISTERED(HttpStatus.BAD_REQUEST, "TOTP 등록이 완료되지 않은 사용자입니다."),
    INVALID_TOTP_CODE(HttpStatus.UNAUTHORIZED, "잘못된 OTP 인증 코드입니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "이메일 인증이 완료되지 않았습니다."),

    //(추후 추가 가능: 이미 존재하는 이메일/아이디 등)
    ;

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
