package com.example.zicdding.global.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum SuccessEnum {
    USER_CREATED(HttpStatus.OK,"회원가입 성공"),
    LOGIN_SUCCESS(HttpStatus.OK, "로그인 성공");

    private final HttpStatus status;
    private final String message;
    public int getStatusCode(){
        return status.value();
    }
}
