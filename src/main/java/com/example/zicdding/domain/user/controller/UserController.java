package com.example.zicdding.domain.user.controller;

import com.example.zicdding.domain.user.dto.*;
import com.example.zicdding.domain.user.entity.User;
import com.example.zicdding.domain.user.service.UserReadService;
import com.example.zicdding.domain.user.service.UserService;
import com.example.zicdding.global.common.enums.SuccessEnum;
import com.example.zicdding.global.common.response.ApiResponse;
import com.example.zicdding.global.util.JwtUtil;
import com.example.zicdding.global.util.RedisUtil;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.prefs.BackingStoreException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    final UserService userService;
    final UserReadService userReadService;
     JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    @Operation(summary = "회원가입")
    @PostMapping("/signUp")
    public ApiResponse<AuthResponseDto> createUser(@RequestBody UserSaveDto userSaveDto) {
        AuthResponseDto user = userService.createUser(userSaveDto);
        return ApiResponse.of(HttpStatus.OK, "로그인 성공",user);
    }

    @Operation(summary = "로그인")
    @PostMapping("/signIn")
    public ApiResponse<AuthResponseDto> signIn(@RequestBody UserLoginDto userLoginDto) throws BackingStoreException {
        // 로그인 처리
        AuthResponseDto authResponse = userService.login(userLoginDto);

        // Set-Cookie 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", "accessToken=" + authResponse.accessToken() + "; HttpOnly; Path=/; SameSite=Strict;");
        headers.add("Set-Cookie", "refreshToken=" + authResponse.refreshToken() + "; HttpOnly; Path=/; SameSite=Strict;");
        return ApiResponse.of(HttpStatus.OK,"로그인 성공", authResponse);
    }
    
    @Operation(summary = "이메일 체크")
    @PostMapping("/checkEmail")
    public ApiResponse<?> checkEmail(@RequestParam("email") String email) {
        userService.checkEmail(email);
        return ApiResponse.of(SuccessEnum.EMAIL_AVAILABLE);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ApiResponse<?> logout(HttpServletRequest response) {
       String email = jwtUtil.getCurrentUserEmail();
        
        //redis 삭제
        redisUtil.delete(email + "_access_token");
        redisUtil.delete(email + "_refresh_token");
        //쿠키 삭제
        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", "accessToken=; HttpOnly; Path=/; Max-Age=0;");
        headers.add("Set-Cookie", "refreshToken=; HttpOnly; Path=/; Max-Age=0;");

        return ApiResponse.of(SuccessEnum.LOGOUT_SUCCESS);
    }
}
