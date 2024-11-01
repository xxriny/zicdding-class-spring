package com.example.zicdding.global.exception;

import com.example.zicdding.global.common.enums.ErrorCodeEnum;
import org.springframework.http.HttpStatus;


public record ErrorResponse(
        HttpStatus httpStatus, // 상태 (예: "error")
        int code,      // 에러 코드
        String message // 에러 메시지
) {

    // ErrorCodeEnum을 기반으로 생성하는 생성자
    public ErrorResponse(ErrorCodeEnum exceptionCode) {
        this(exceptionCode.getHttpStatus(), exceptionCode.getCode(), exceptionCode.getMessage());
    }
    // ErrorResponse 객체를 생성하는 정적 팩토리 메서드
    public static ErrorResponse of(ErrorCodeEnum code) {
        return new ErrorResponse(code);
    }

    // ErrorResponse 객체를 생성하는 정적 메서드
    public static ErrorResponse error(CustomException e) {
        return new ErrorResponse(e.getHttpStatus(), e.getErrorCode().getCode(), e.getMessage());
    }
}