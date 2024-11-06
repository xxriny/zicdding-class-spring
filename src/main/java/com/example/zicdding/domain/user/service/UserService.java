package com.example.zicdding.domain.user.service;

import com.example.zicdding.domain.user.dto.*;
import com.example.zicdding.domain.user.entity.User;
import com.example.zicdding.domain.user.repository.UserRepository;
import com.example.zicdding.global.common.enums.ErrorCodeEnum;

import com.example.zicdding.global.exception.CustomException;

import com.example.zicdding.security.CustomUserDetail;
import com.example.zicdding.security.provider.JwtProvider;
import com.example.zicdding.global.util.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

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
    final private UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate; // RedisTemplate 추가
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    /**
     유저 생성
     @param userSaveDto
     @return 유저 생성 성공, accesstoken, refreshtoken
      * */
    public AuthResponseDto createUser(UserSaveDto userSaveDto) {
        //1. 이메일 체크
        checkEmail(userSaveDto.email());
        //2. 패스워드 인코딩
        String encryptPassword = passwordService.encryptPassword(userSaveDto.password());
        //저장
        var user  = User.builder()
                .email(userSaveDto.email())
                .nickname(userSaveDto.nickname())
                .password(encryptPassword)
                .build();
        String refreshToken = String.valueOf(jwtProvider.generateRefreshToken(user));
        String accessToken =  String.valueOf(jwtProvider.generateRefreshToken(user));
        saveAccessToken(user);
        saveRefreshToken(user);
        userRepository.save(user);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userSaveDto.email(),userSaveDto.password());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        return buildAuthResponse(user, accessToken, refreshToken);
    }
    /**
     사용자 email 체크
     @param email
     @return 이메일 중복 여부
      * */
    public void checkEmail(String email) {
        Optional<User> emailCheck = userRepository.findByEmail(email);
        if (emailCheck.isPresent()) {
            throw new CustomException(EMAIL_DUPLICATE);
        }
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

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userLoginDto.email(), userLoginDto.password());
        System.out.println("authenti" + authenticationToken);
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        String email = authentication.getName();
        System.out.println("이메일" +  email);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String existingAccessToken = getAccessToken(email);
        if (existingAccessToken != null && jwtProvider.validateToken(existingAccessToken)) {
            redisTemplate.delete(email + "_access_token"); // 기존 액세스 토큰 삭제
        }
        // 새로운 액세스 토큰과 리프레시 토큰 생성
        String accessToken = saveAccessToken(user);
        String refreshToken = user.getRefreshToken();
        System.out.println("인증" + authentication);
        return buildAuthResponse(user, accessToken, refreshToken);
//        if (accessToken != null && jwtProvider.validateToken(accessToken)) {
//            redisTemplate.opsForValue().set(user.getEmail() + "_refresh_token", refreshToken, 30, TimeUnit.DAYS);
//            return buildAuthResponse(user, accessToken, refreshToken);
//        }
//
//        if(accessToken == null && !jwtProvider.validateToken(accessToken)) {
//
//            redisTemplate.opsForValue().set(user.getEmail() + "_refresh_token", refreshToken, 30, TimeUnit.DAYS);
//             authentication = new UsernamePasswordAuthenticationToken(user, null, null);
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//            return buildAuthResponse(user, accessToken, refreshToken);
//        }
//        throw new CustomException(INVALID_ACCESS_TOKEN);
        }

    public String refreshAccessToken(String email) {
         User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCodeEnum.USER_NOT_FOUND));
        return saveAccessToken(user);
    }
    /**
     * 액세스 토큰 생성 및 저장
     *
     * @param user 유저 객체
     * @return 생성된 액세스 토큰
     */
    private String saveAccessToken(User user) {
        String accessToken = jwtProvider.generateAccessToken(user).accessToken();

        redisTemplate.opsForValue().set(user.getEmail() + "_access_token", accessToken, 1, TimeUnit.HOURS);
        return accessToken;
    }
    /**
     * 리프레시 토큰 생성 및 저장
     *
     * @param user 유저 객체
     * @return 생성된 리프레시 토큰
     */
    private String saveRefreshToken(User user) {
        String refreshToken = jwtProvider.generateRefreshToken(user).refreshToken();
        redisTemplate.opsForValue().set(user.getEmail() + "_refresh_token", refreshToken, 30, TimeUnit.DAYS);
        user.setRefreshToken(refreshToken);
        return refreshToken;
    }
    /**
     * 리프레시 토큰으로 DB에서 이메일 찾기
     *
     * @param refreshToken  리프레시 토큰
     * @return AuthResponseDto
     */
    public String getEmailFromRefreshToken(String refreshToken) {
        Optional<User> userOptional = userRepository.findByRefreshToken(refreshToken);
        return userOptional.map(User::getEmail).orElseThrow(() -> new CustomException(ErrorCodeEnum.USER_NOT_FOUND));
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
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String getAccessToken(String email) {
        System.out.println(email);
        return redisTemplate.opsForValue().get(email + "_access_token");
    }

    public String getRefreshToken(String email) {
        return redisTemplate.opsForValue().get(email + "_refresh_token");
    }

}
