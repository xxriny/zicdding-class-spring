package com.example.zicdding.global.exception.dto;

import com.example.zicdding.global.common.enums.ErrorCodeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ExceptionDto {
    private final HttpStatus statusCode;

    @NotNull
    private final Integer code;

    @NotNull
    private final String message;

    public ExceptionDto(ErrorCodeEnum exceptionEnum) {
        this.statusCode = exceptionEnum.getHttpStatus();
        this.code = exceptionEnum.getCode();
        this.message = exceptionEnum.getMessage();
    }
    public static ExceptionDto of(ErrorCodeEnum exceptionEnum) {
        return new ExceptionDto(exceptionEnum);
    }
}
