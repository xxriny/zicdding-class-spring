package com.example.zicdding.config.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public enum ApiExceptionEnum {
    RUNTIME_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "E0001")
    , ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "E0002", "인증되지 않은 사용자입니다.")
    , FORBIDDEN(HttpStatus.FORBIDDEN, "50001", "권한이 없습니다.");

    private HttpStatus httpStatus;
    private String code;
    private String message;

    ApiExceptionEnum(HttpStatus httpStatus, String code){
        this.httpStatus = httpStatus;
        this.code = code;
    }
    ApiExceptionEnum(HttpStatus httpStatus, String code, String message){
        this.httpStatus = httpStatus;
                this.code   = code;
                this.message = message;
    }
}
