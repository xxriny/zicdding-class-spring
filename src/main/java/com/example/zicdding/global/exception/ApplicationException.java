package com.example.zicdding.global.exception;

import com.example.zicdding.global.common.enums.ErrorCodeEnum;
import org.springframework.http.HttpStatus;

public class ApplicationException extends RuntimeException {
    private final ErrorCodeEnum errorCodeEnum;

    public ApplicationException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getMessage()); // 기본 메시지를 부모 클래스에 전달
        this.errorCodeEnum = errorCodeEnum;
    }

    public ErrorCodeEnum getApiExceptionEnum() {
        return errorCodeEnum;
    }

    public HttpStatus getHttpStatus() {
        return errorCodeEnum.getHttpStatus();
    }

    public Integer getCode() {
        return errorCodeEnum.getCode();
    }
}