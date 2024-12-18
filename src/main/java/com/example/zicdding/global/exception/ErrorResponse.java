package com.example.zicdding.global.exception;

import com.example.zicdding.global.common.enums.ErrorCodeEnum;
import org.springframework.http.HttpStatus;


public record ErrorResponse(
        HttpStatus code,  // 상캐 CONFLICT
        int status, //HTTPSTATUS
        String message // 에러 메시지
) {

    public static ErrorResponse error(CustomException e) {
        return new ErrorResponse(e.getErrorCode().getCode(), e.getHttpStatus().value(),  e.getMessage());
    }
}