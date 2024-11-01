package com.example.zicdding.global.exception.dto;

import com.example.zicdding.global.common.enums.ErrorCodeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ExceptionDto {
    private final int status;

    private final HttpStatus code;

    @NotNull
    private final String message;

    public ExceptionDto(ErrorCodeEnum exceptionEnum) {
        this.status = exceptionEnum.getStatus();
        this.code = exceptionEnum.getCode();
        this.message = exceptionEnum.getMessage();
    }

}
