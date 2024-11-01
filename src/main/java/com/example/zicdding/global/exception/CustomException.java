package com.example.zicdding.global.exception;

import com.example.zicdding.global.common.enums.ErrorCodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCodeEnum errorCode;

    public HttpStatus getHttpStatus() {
        return errorCode.getCode();
    }

    public CustomException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getMessage()); // 기본 메시지를 부모 클래스에 전달
        this.errorCode = errorCodeEnum;
    }
}