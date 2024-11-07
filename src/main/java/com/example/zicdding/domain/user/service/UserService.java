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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private final RedisUtil redisUtil;

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

        var user  = User.builder()
                .email(userSaveDto.email())
                .nickname(userSaveDto.nickname())
                .password(encryptPassword)
                .build();
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userSaveDto.email(),userSaveDto.password());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        String accessToken = jwtProvider.generateAccessToken(authenticationToken);
        jwtProvider.generateRefreshToken(authenticationToken);
        String refreshToken = jwtProvider.getRefreshTokenFromRedis(user.getEmail());

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        //SecurityContext에 인증 정보 저장
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
        //인증 토큰 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userLoginDto.email(), userLoginDto.password());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = redisTemplate.opsForValue().get(userLoginDto.email());
        if(refreshToken == null && !jwtProvider.validateToken(refreshToken)) {
            System.out.println("Refresh token expired");
            jwtProvider.generateRefreshToken(authentication);
            refreshToken = redisUtil.get(userLoginDto.email());
           }
        System.out.println(refreshToken);
        return buildAuthResponse(user, accessToken,refreshToken);
    }

    public AuthResponseDto reissueAccessToken(String refreshToken){
        System.out.println(refreshToken + "왜");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication.getName());
        System.out.println(redisTemplate.opsForValue().get(authentication.getName()));
           if(refreshToken.equals(redisUtil.get(authentication.getName()))) {

               System.out.println(authentication.getName()  +" 이메일");
                User user = (User) authentication.getPrincipal();
                String accessToken = jwtProvider.generateAccessToken(authentication);
                return  buildAuthResponse(user,accessToken,refreshToken );
            }else{
                throw new RuntimeException("요청한 리프레시 없음");
            }
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
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

}
