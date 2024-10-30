package com.example.zicdding.global.common.response;

import com.example.zicdding.global.common.enums.SuccessEnum;
import com.example.zicdding.global.exception.dto.ExceptionDto;
import com.example.zicdding.global.exception.CustomException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micrometer.common.lang.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public record ApiResponse<T>(
        int status,
        String message,
        @Nullable T data,
        @Nullable ExceptionDto error
) {
    public static ResponseEntity<ApiResponse>  of(SuccessEnum successEnum) {
        return  ResponseEntity.status(successEnum.getStatus())
                .body(new ApiResponse(successEnum.getStatusCode(), successEnum.getMessage(), null, null ));
    }

    public static <T> ResponseEntity<ApiResponse<T>> of(String message) {
        return new ApiResponse<>(HttpStatus.CREATED.value(), HttpStatu, true, message, null, null);
    }

    public static <T> ApiResponse<T> error(String message, final CustomException e) {
        ExceptionDto errorData = ExceptionDto.of(e.getErrorCode());
        HttpStatus errorStatus = e.getErrorCode().getHttpStatus();
        return new ApiResponse<>(errorStatus.value(), errorStatus, false, message, null, errorData);
    }


}

