package com.example.zicdding.domain.user.service;

import com.example.zicdding.domain.user.dto.*;
import com.example.zicdding.domain.user.entity.User;
import com.example.zicdding.domain.user.repository.UserRepository;
import com.example.zicdding.global.common.enums.ErrorCodeEnum;
import com.example.zicdding.global.common.enums.SuccessEnum;
import com.example.zicdding.global.common.response.ApiResponse;
import com.example.zicdding.global.exception.CustomException;
import com.example.zicdding.global.exception.ErrorResponse;
import com.example.zicdding.security.provider.JwtProvider;
import com.example.zicdding.global.util.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;

import static com.example.zicdding.global.common.enums.ErrorCodeEnum.EMAIL_DUPLICATE;
import static com.example.zicdding.global.common.enums.ErrorCodeEnum.PASSWORD_NOT_MATCH;


@RequiredArgsConstructor
@Service
public class UserService {
    final private UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate; // RedisTemplate 추가

    /**
     유저 생성
     @param userSaveDto
     @return 유저 생성 성공, accesstoken, refreshtoken
      * */
    public AuthResponseDto createUser(UserSaveDto userSaveDto) {
        checkEmail(userSaveDto.email());
        String encryptPassword = passwordService.encryptPassword(userSaveDto.password());

        var user  = User.builder()
                .email(userSaveDto.email())
                .nickname(userSaveDto.nickname())
                .password(encryptPassword)
                .phoneNumber(userSaveDto.phoneNumber())
                .build();

        JwtDto refreshTokenDto = jwtProvider.generateRefreshToken(user);
        String refreshToken = refreshTokenDto.refreshToken();
        user = user.toBuilder()
                .refreshToken(refreshToken)
                .build();


        User savedUser = userRepository.save(user);
        JwtDto accessTokenDto = jwtProvider.generateAccessToken(user);
        String accessToken = accessTokenDto.accessToken();
        redisTemplate.opsForValue().set(user.getEmail() + "_access_token", accessToken, 1, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(user.getEmail() + "_refresh_token", refreshToken, 1, TimeUnit.DAYS);

        return AuthResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
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

    public AuthResponseDto login(UserLoginDto userLoginDto) throws BackingStoreException {
        Optional<User> userOptional = userRepository.findByEmail(userLoginDto.email());
        User user = userOptional.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        if(!passwordEncoder.matches(userLoginDto.password(), user.getPassword())) {
            throw new CustomException(PASSWORD_NOT_MATCH);
        }

    //redis에 저장한 accesstoken 가져오기
        String accessToken =getAccessToken(user.getEmail());

        if (accessToken != null && jwtProvider.validateToken(accessToken)) {
            String refreshToken = redisTemplate.opsForValue().get(userLoginDto.email() + "_refresh_token");
            // 유효한 토큰이 있는 경우
            return AuthResponseDto.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        }
        JwtDto accessTokenDto = jwtProvider.generateAccessToken(user);
        accessToken = accessTokenDto.accessToken();
        redisTemplate.opsForValue().set(user.getEmail() + "_access_token", accessToken, 1, TimeUnit.HOURS);

        // 새로운 refreshToken 생성 (필요한 경우)
        String refreshToken = getRefreshToken(user.getEmail());
        if (refreshToken == null) {
            JwtDto refreshTokenDto = jwtProvider.generateRefreshToken(user);
            refreshToken = refreshTokenDto.refreshToken();
            redisTemplate.opsForValue().set(userLoginDto.email() + "_refresh_token", refreshToken, 1, TimeUnit.DAYS);
        }

        // AuthResponseDto 생성 및 반환
        return AuthResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String getAccessToken(String email) {
        return redisTemplate.opsForValue().get(email + "_access_token");
    }
    public String getRefreshToken(String email) {
        return redisTemplate.opsForValue().get(email + "_refresh_token");
    }

}
