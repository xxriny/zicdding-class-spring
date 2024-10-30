package com.example.zicdding.domain.user.service;

import com.example.zicdding.domain.user.dto.*;
import com.example.zicdding.domain.user.entity.User;
import com.example.zicdding.domain.user.repository.UserRepository;
import com.example.zicdding.global.exception.CustomException;
import com.example.zicdding.global.common.enums.ErrorCodeEnum;
import com.example.zicdding.security.provider.JwtProvider;
import com.example.zicdding.global.util.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;



@RequiredArgsConstructor
@Service
public class UserService {
    final private UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate; // RedisTemplate 추가


    public User createUser(UserSaveDto userSaveDto) {
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
        return  savedUser;
    }


    public void checkEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new CustomException(ErrorCodeEnum.EMAIL_DUPLICATE);
        }
    }

    public AuthResponseDto login(UserLoginDto userLoginDto) throws BackingStoreException {
        Optional<User> userOptional = userRepository.findByEmail(userLoginDto.email());
        User user = userOptional.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        if(!passwordEncoder.matches(userLoginDto.password(), user.getPassword())) {
            throw new BackingStoreException("비밀번호 잘못됨");
        }

    //redis에 저장한 accesstoken 가져오기
        String accessToken =getAccessToken(user.getEmail());


        if (accessToken != null && jwtProvider.validateToken(accessToken)) {
            String refreshToken = redisTemplate.opsForValue().get(userLoginDto.email() + "_refresh_token");
            // 유효한 토큰이 있는 경우
            return AuthResponseDto.builder()
                    .user(new UserDto(user.getId(), user.getEmail(), user.getNickname(), user.getPhoneNumber()))
                    .jwt(JwtDto.builder()
                            .grantType("Bearer")
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .build())
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
        return  AuthResponseDto.builder()
                .user(new UserDto(user.getId(), user.getEmail(), user.getNickname(), user.getPhoneNumber()))
                .jwt(JwtDto.builder()
                        .grantType("Bearer")
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build())
                .build();
    }



    // 로그아웃 메서드


    public String getAccessToken(String email) {
        return redisTemplate.opsForValue().get(email + "_access_token");
    }
    public String getRefreshToken(String email) {
        return redisTemplate.opsForValue().get(email + "_refresh_token");
    }


}
