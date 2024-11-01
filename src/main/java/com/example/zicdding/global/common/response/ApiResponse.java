package com.example.zicdding.global.common.response;

import com.example.zicdding.global.common.enums.SuccessEnum;
import io.micrometer.common.lang.Nullable;
import org.springframework.http.HttpStatus;


public record ApiResponse<T>(
        int status,
        String message,
        @Nullable T data
) {
    // SUCCESSENUM
    public static <T> ApiResponse<T> of(SuccessEnum successEnum) {
        return new ApiResponse<>(successEnum.getStatus().value(), successEnum.getMessage(), null);
    }

    // 커스텀 메시지
    public static <T> ApiResponse<T> of(HttpStatus status, String message) {
        return new ApiResponse<>(status.value(), message, null);
    }

    // 커스텀 메시지 + DATA
    public static <T> ApiResponse<T> of(HttpStatus status, String message, T data) {
        return new ApiResponse<>(status.value(), message, data);
    }

}
