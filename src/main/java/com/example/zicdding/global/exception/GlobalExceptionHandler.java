package com.example.zicdding.global.exception;

import com.example.zicdding.global.common.enums.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 404 또는 HTTP 메서드 오류를 위한 처리
    @ExceptionHandler({NoHandlerFoundException.class, HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<ErrorResponse> handleNoPageFoundException(Exception e) {
        log.error("NoHandlerFoundException or HttpRequestMethodNotSupportedException: {}", e.getMessage(), e);
        CustomException exception = new CustomException(ErrorCodeEnum.NOT_FOUND_END_POINT);
        return ResponseEntity.status(exception.getHttpStatus())
                .body(ErrorResponse.error(exception));
    }
    // CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("CustomException: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getHttpStatus())
                .body(ErrorResponse.error(e));
    }
    // 그 외 Exception 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Exception: {}", e.getMessage(), e);
        CustomException exception = new CustomException(ErrorCodeEnum.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(exception.getHttpStatus())
                .body(ErrorResponse.error(exception));
    }
}
