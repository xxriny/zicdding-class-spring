package com.example.zicdding.security.handler;

import com.example.zicdding.config.exception.enums.ApiExceptionEnum;
import org.springframework.http.HttpStatus;

public class ApplicationException extends RuntimeException {
    private final ApiExceptionEnum apiExceptionEnum;

    public ApplicationException(ApiExceptionEnum apiExceptionEnum) {
        super(apiExceptionEnum.getMessage()); // 기본 메시지를 부모 클래스에 전달
        this.apiExceptionEnum = apiExceptionEnum;
    }

    public ApiExceptionEnum getApiExceptionEnum() {
        return apiExceptionEnum;
    }

    public HttpStatus getHttpStatus() {
        return apiExceptionEnum.getHttpStatus();
    }

    public String getCode() {
        return apiExceptionEnum.getCode();
    }
}