package com.example.zicdding.domain.user.service;

import com.example.zicdding.domain.user.dto.*;
import com.example.zicdding.domain.user.entity.User;
import com.example.zicdding.domain.user.repository.UserRepository;
import com.example.zicdding.global.common.enums.ErrorCodeEnum;

import com.example.zicdding.global.common.response.ApiResponse;
import com.example.zicdding.global.exception.CustomException;

import com.example.zicdding.global.util.RedisUtil;
import com.example.zicdding.security.provider.JwtProvider;
import com.example.zicdding.global.util.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractUserDetailsReactiveAuthenticationManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.example.zicdding.global.common.enums.ErrorCodeEnum.*;


@RequiredArgsConstructor
@Service
public class UserService {
    private final AuthenticationManager authenticationManager;
    final private UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate; // RedisTemplate 추가
    private final RedisUtil redisUtil;
    /**
     유저 생성
     @param userSaveDto
     @return user, accesstoken, refreshtoken
      * */
    public AuthResponseDto createUser(UserSaveDto userSaveDto) {
        //1. 이메일 체크
        checkEmail(userSaveDto.email());
        //2. 패스워드 인코딩
        String encryptPassword = passwordService.encryptPassword(userSaveDto.password());
        // 3. 유저 생성
        var user  = User.builder()
                .email(userSaveDto.email())
                .nickname(userSaveDto.nickname())
                .password(encryptPassword)
                .build();
        // 4. 인증 토큰 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userSaveDto.email(),userSaveDto.password());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        String accessToken = jwtProvider.generateAccessToken(authenticationToken);
        String refreshToken = jwtProvider.generateRefreshToken(authenticationToken);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);
         // 저장된 ID 확인
        System.out.println("Saved User Email: " + user.getId());
        //SecurityContext에 인증 정보 저장
        return buildAuthResponse(user, accessToken, refreshToken);
    }
    /**
     사용자 email 체크
     @param email
     @return 이메일 중복 여부
      * */
    public void checkEmail(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            throw new CustomException(EMAIL_DUPLICATE);
        });
    }

    /**
     *  로그인
     *
     * @param userLoginDto 로그인 정보 DTO
     * @return 로그인 성공 시 유저 정보와 토큰
     */
    public AuthResponseDto login(UserLoginDto userLoginDto) {

        User user = userRepository.findByEmail(userLoginDto.email())
                .orElseThrow(() -> new CustomException(ErrorCodeEnum.USER_NOT_FOUND));

        if (!passwordEncoder.matches(userLoginDto.password(), user.getPassword())) {
            throw new CustomException(PASSWORD_NOT_MATCH);
        }

        //인증 토큰 생성

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDto.email(), userLoginDto.password()));

        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.getRefreshTokenFromRedis(user.getRefreshToken());

        if (refreshToken == null) {
            // 리프레시 토큰이 없을 경우, 새로운 리프레시 토큰 생성
            String newRefreshToken = jwtProvider.generateRefreshToken(authentication);
            return buildAuthResponse(user, accessToken, newRefreshToken);
        }

        System.out.println(refreshToken);
        System.out.println(refreshToken +" ==========================로그인 시 확인" + authentication.getName()  + "dasdasdadsadsas=============");

        return buildAuthResponse(user, accessToken,refreshToken);
    }

    /**
     * 리프레시 토큰을 사용해 새로운 액세스 토큰을 발급
     * @param refreshToken 리프레시 토큰
     * @return 새로 발급된 액세스 토큰과 리프레시 토큰
     */
    public AuthResponseDto reissueAccessToken(String refreshToken){
        Authentication authentication = jwtProvider.getAuthentication(refreshToken);
        String email = authentication.getName();

        String redisRefreshToken = redisUtil.get(email);
        if (redisRefreshToken == null || !redisRefreshToken.equals(refreshToken)) {
            throw new CustomException(ErrorCodeEnum.INVALID_REFRESH_TOKEN);
        }
        String newAccessToken = jwtProvider.generateAccessToken(authentication);
        // 사용자 정보 반환
        User user = userRepository.findByRefreshToken(refreshToken).orElseThrow(() -> new CustomException(ErrorCodeEnum.USER_NOT_FOUND));
        return buildAuthResponse(user, newAccessToken, refreshToken);
    }


    /**
     * 인증된 유저 정보를 반환하는 메소드
     *
     * @param user          유저 객체
     * @param accessToken   액세스 토큰
     * @param refreshToken  리프레시 토큰
     * @return AuthResponseDto
     */
    private AuthResponseDto buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

}
