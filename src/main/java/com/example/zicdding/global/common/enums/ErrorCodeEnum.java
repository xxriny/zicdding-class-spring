package com.example.zicdding.global.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
@AllArgsConstructor
@Getter
public enum ErrorCodeEnum {
    RUNTIME_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, 100),
    ACCESS_DENIED(HttpStatus.UNAUTHORIZED, 40101, "인증되지 않은 사용자입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, 40301, "권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 40401, "해당 유저를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 50000, "서버 내부 오류입니다."),
    NOT_FOUND_END_POINT(HttpStatus.NOT_FOUND, 40400, "존재하지 않는 API입니다."),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, 40901, "이메일이 이미 사용 중입니다."),
    PASSWORD_NOT_MATCH(HttpStatus.NOT_FOUND, 40901, "비밀번호가 틀립니다.");

    private final HttpStatus code;
    private final Integer status;
    private final String message;

    //status와 코드만
    ErrorCodeEnum(HttpStatus code, Integer status) {
        this(code, status, null);
    }
}