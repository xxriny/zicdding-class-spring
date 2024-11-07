package com.example.zicdding.domain.user.controller;

import com.example.zicdding.domain.user.dto.*;
import com.example.zicdding.domain.user.entity.User;
import com.example.zicdding.domain.user.service.CustomUserDetailService;
import com.example.zicdding.domain.user.service.UserReadService;
import com.example.zicdding.domain.user.service.UserService;
import com.example.zicdding.global.common.enums.SuccessEnum;
import com.example.zicdding.global.common.response.ApiResponse;
import com.example.zicdding.global.util.JwtUtil;
import com.example.zicdding.global.util.RedisUtil;

import com.example.zicdding.security.CustomUserDetail;
import com.example.zicdding.security.filter.JwtAuthFilter;
import com.example.zicdding.security.provider.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;


@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final RedisUtil redisUtil;
    private final JwtAuthFilter jwtAuthFilter;
    private final JwtProvider jwtProvider;
    @Operation(summary = "회원가입")
    @PostMapping("/signUp")
    public ApiResponse<AuthResponseDto> createUser(@RequestBody UserSaveDto userSaveDto, HttpServletResponse response) {
        AuthResponseDto user = userService.createUser(userSaveDto);
        response.setHeader("Set-Cookie", "accessToken=" + user.accessToken() + "; HttpOnly; Path=/; SameSite=None; Secure;");
        response.setHeader("Set-Cookie", "refreshToken=" + user.refreshToken() + ";HttpOnly; Path=/; SameSite=None; Secure;");
        System.out.println("로그인" + user.accessToken());
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
    @GetMapping("/reissue")
    public ApiResponse<AuthResponseDto> reissue(HttpServletRequest request) {
        String refreshToken = jwtAuthFilter.resolveRefreshToken(request);
        System.out.println("refreshtOKEN       " + refreshToken);
        AuthResponseDto newAccessToken = userService.reissueAccessToken(refreshToken);
        return ApiResponse.of(HttpStatus.OK,"재발급 완료",newAccessToken);
    }
    @Operation(summary = "이메일 체크")
    @PostMapping("/checkEmail")
    public ApiResponse<?> checkEmail(@RequestParam("email") String email) {
        userService.checkEmail(email);
        return ApiResponse.of(SuccessEnum.EMAIL_AVAILABLE);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ApiResponse<?> logout(HttpServletRequest request) {
            String accessToken = jwtAuthFilter.resolveAccessToken(request);
            String email = jwtProvider.getEmailFromToken(accessToken);
            if(redisUtil.get(email) != null){
                redisUtil.delete(email);
            }
            redisUtil.setBlackList(accessToken, "blackList", Duration.ofDays(100000));
        return ApiResponse.of(SuccessEnum.LOGOUT_SUCCESS);
    }

}
