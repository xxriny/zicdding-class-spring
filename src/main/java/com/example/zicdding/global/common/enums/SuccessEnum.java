package com.example.zicdding.global.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum SuccessEnum {
    USER_CREATED(HttpStatus.OK,"회원가입 성공"),
    LOGIN_SUCCESS(HttpStatus.OK, "로그인 성공"),
    EMAIL_AVAILABLE(HttpStatus.OK,"사용가능한 이메일입니다"),
    LOGOUT_SUCCESS(HttpStatus.OK,"로그아웃 되었습니다"),
    MYPAGE_SUCCESS(HttpStatus.OK, "마이페이지 조회 성공");
    private final HttpStatus status;
    private final String message;
}
