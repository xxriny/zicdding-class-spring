package com.example.zicdding.domain.user.controller;

import com.example.zicdding.domain.user.dto.*;
import com.example.zicdding.domain.user.service.UserReadService;
import com.example.zicdding.domain.user.service.UserService;
import com.example.zicdding.global.common.enums.ErrorCodeEnum;
import com.example.zicdding.global.common.enums.SuccessEnum;
import com.example.zicdding.global.common.response.ApiResponse;
import com.example.zicdding.domain.file.service.FileService;
import com.example.zicdding.global.util.RedisUtil;

import com.example.zicdding.security.filter.JwtAuthFilter;
import com.example.zicdding.security.provider.JwtProvider;
import io.minio.errors.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.prefs.BackingStoreException;


@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final RedisUtil redisUtil;
    private final JwtAuthFilter jwtAuthFilter;
    private final JwtProvider jwtProvider;
    private final UserReadService userReadService;
    private final FileService fileService;

    @Operation(summary = "회원가입")
    @PostMapping("/signUp")
    public ApiResponse<AuthResponseDto> createUser(@RequestBody UserSaveDto userSaveDto, HttpServletResponse response) {
        AuthResponseDto authResponse = userService.createUser(userSaveDto);

        response.addCookie(createCookie("accessToken" , authResponse.accessToken()));
        response.addCookie(createCookie("refreshToken" , authResponse.accessToken()));

        return ApiResponse.of(SuccessEnum.USER_CREATED, authResponse);
    }
    // 인증된 사용자 정보 조회
    @GetMapping("/me")
    public ApiResponse<UserInfoDto> getAuthenticatedUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ApiResponse.of(ErrorCodeEnum.ACCESS_DENIED);
        }

        // 현재 로그인된 사용자 정보를 가져옴
        UserInfoDto userResponse = userReadService.getUserInfo(userDetails.getUsername());
        return ApiResponse.of(SuccessEnum.MYPAGE_SUCCESS,userResponse);
    }
    @PostMapping("/me")
    public ApiResponse<String> updateAuthenticatedUser(@RequestParam("file") MultipartFile file ,@AuthenticationPrincipal UserDetails userDetails) {
//        try{
//         //   String fileName = fileService.uploadFile(file);
//        //    String imageUrl = fileService.getImageUrl(fileName);
//          //  userService.updateUser(userDetails.getUsername(), imageUrl);
//            return ApiResponse.of(SuccessEnum.MYPAGE_SUCCESS);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (ServerException e) {
//            throw new RuntimeException(e);
//        } catch (InsufficientDataException e) {
//            throw new RuntimeException(e);
//        } catch (ErrorResponseException e) {
//            throw new RuntimeException(e);
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        } catch (InvalidKeyException e) {
//            throw new RuntimeException(e);
//        } catch (InvalidResponseException e) {
//            throw new RuntimeException(e);
//        } catch (XmlParserException e) {
//            throw new RuntimeException(e);
//        } catch (InternalException e) {
//            throw new RuntimeException(e);
//        }
        return null;
    }
    @Operation(summary = "로그인")
    @PostMapping("/signIn")
    public ApiResponse<AuthResponseDto> signIn(@RequestBody UserLoginDto userLoginDto, HttpServletResponse response) throws BackingStoreException {
        // 로그인 처리
        AuthResponseDto authResponse = userService.login(userLoginDto);

        response.addCookie(createCookie("accessToken", authResponse.accessToken()));
        response.addCookie(createCookie("refreshToken", authResponse.refreshToken()));

        return ApiResponse.of(SuccessEnum.LOGIN_SUCCESS, authResponse);
    }

    @GetMapping("/reissue")
    public ApiResponse<AuthResponseDto> reissue(@CookieValue(name ="refreshToken") String request, HttpServletResponse response) {
        AuthResponseDto authResponse = userService.reissueAccessToken(request);
        // 기존 쿠키 삭제
        deleteCookie(response, "accessToken");
        deleteCookie(response, "refreshToken");

        // 새 쿠키 설정
        response.addCookie(createCookie("accessToken", authResponse.accessToken()));
        response.addCookie(createCookie("refreshToken", authResponse.refreshToken()));

        return ApiResponse.of(HttpStatus.OK,"재발급 완료",authResponse);
    }
    
    @Operation(summary = "이메일 체크")
    @PostMapping("/checkEmail")
    public ApiResponse<?> checkEmail(@RequestParam("email") String email) {
        userService.checkEmail(email);
        return ApiResponse.of(SuccessEnum.EMAIL_AVAILABLE);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ApiResponse<?> logout(HttpServletRequest request ,HttpServletResponse response) {
        String accessToken = jwtAuthFilter.resolveAccessToken(request);
        if (accessToken == null || accessToken.isEmpty()) {
            return ApiResponse.of(ErrorCodeEnum.ACCESS_DENIED);
        }

        Authentication authentication = jwtProvider.getAuthentication(accessToken);
        if (authentication == null) {
            return ApiResponse.of(ErrorCodeEnum.ACCESS_DENIED);
        }

        String email = authentication.getName();

        if(redisUtil.get(email) != null){
            redisUtil.delete(email);
        }

        redisUtil.setBlackList(accessToken, "accessToken", Duration.ofDays(3000));


        SecurityContextHolder.clearContext();

        deleteCookie(response, "accessToken");
        deleteCookie(response, "refreshToken");

        // 로그아웃 성공 응답
        return ApiResponse.of(SuccessEnum.LOGOUT_SUCCESS);
    }
    
    //쿠키 삭제
    private void deleteCookie( HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setHttpOnly(true);  // 보안 설정
        cookie.setSecure(true);    // HTTPS에서만 작동하도록 설정
        cookie.setPath("/");       // 전체 경로에서 쿠키를 삭제하도록 설정
        cookie.setMaxAge(0);       // 만료 시간 0으로 설정하여 쿠키 삭제
        response.addCookie(cookie);
    }
    // 쿠키 생성
    private Cookie createCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");      
        cookie.setMaxAge(3600);   // 만료 시간 1시간
        return cookie;
    }
}
