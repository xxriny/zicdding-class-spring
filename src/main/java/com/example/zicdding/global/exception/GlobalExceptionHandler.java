package com.example.zicdding.global.exception;

import com.example.zicdding.global.common.enums.ErrorCodeEnum;
import com.example.zicdding.global.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = {NoHandlerFoundException.class, HttpRequestMethodNotSupportedException.class})
    public ApiResponse handleNoPageFoundException(Exception e) {
        log.error(e.getMessage(), e);
        return ApiResponse.error(new CustomException(ErrorCodeEnum.NOT_FOUND_END_POINT));
    }
    @ExceptionHandler(value = {CustomException.class})
    public ApiResponse handleCustomException(CustomException e) {
        log.error(e.getMessage(), e);
        return ApiResponse.error(e);
    }
    @ExceptionHandler(value = {Exception.class})
    public ApiResponse handleException(Exception e) {
        log.error(e.getMessage(), e);
        return ApiResponse.error(new CustomException(ErrorCodeEnum.INTERNAL_SERVER_ERROR));
    }
}
