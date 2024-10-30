package com.example.zicdding.domain.user.controller;

import com.example.zicdding.domain.user.dto.*;
import com.example.zicdding.domain.user.entity.User;
import com.example.zicdding.domain.user.service.UserReadService;
import com.example.zicdding.domain.user.service.UserService;
import com.example.zicdding.global.common.response.ApiResponse;
import com.example.zicdding.global.util.JwtUtil;
import com.example.zicdding.global.util.RedisUtil;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody UserSaveDto UserSaveDto) {
        var user = userService.createUser(UserSaveDto);
        return ResponseEntity.ok(ApiResponse.created("회원가입에 성공했습니다."));
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> signIn(@RequestBody UserLoginDto userLoginDto) throws BackingStoreException {
        // 로그인 처리
        AuthResponseDto authResponse = userService.login(userLoginDto);

        // Set-Cookie 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", "accessToken=" + authResponse.jwt().accessToken() + "; HttpOnly; Path=/; SameSite=Strict;");
        headers.add("Set-Cookie", "refreshToken=" + authResponse.jwt().refreshToken() + "; HttpOnly; Path=/; SameSite=Strict;");
        return ResponseEntity.ok()
                .headers(headers)
                .body(ApiResponse.success("로그인 성공",authResponse));
    }
    @PostMapping("/checkEmail")
    public ResponseEntity<ApiResponse> checkEmail(@RequestBody String email) {
        userService.checkEmail(email);
        return ResponseEntity.ok(ApiResponse.success("사용가능한 이메일입니다.",null));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest response) {
       String email = jwtUtil.getCurrentUserEmail();
        // Redis에서 토큰 삭제
        redisUtil.delete(email + "_access_token");
        redisUtil.delete(email + "_refresh_token");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", "accessToken=; HttpOnly; Path=/; Max-Age=0;");
        headers.add("Set-Cookie", "refreshToken=; HttpOnly; Path=/; Max-Age=0;");

        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공"));
    }
}