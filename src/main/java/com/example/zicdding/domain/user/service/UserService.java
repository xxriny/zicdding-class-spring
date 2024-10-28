package com.example.zicdding.domain.user.service;

import com.example.zicdding.domain.user.dto.JwtDto;
import com.example.zicdding.domain.user.dto.UserSaveDto;
import com.example.zicdding.domain.user.entity.User;
import com.example.zicdding.domain.user.repository.UserRepository;
import com.example.zicdding.security.JwtProvider;
import com.example.zicdding.util.PasswordService;
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
    private final UserReadService userReadService;
    private final RedisTemplate<String, String> redisTemplate; // RedisTemplate 추가


    public User signUp(UserSaveDto userSaveDto) {
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

        userRepository.save(user);

        JwtDto accessTokenDto = jwtProvider.generateAccessToken(user);
        String accessToken = accessTokenDto.accessToken();
        redisTemplate.opsForValue().set(user.getEmail() + "_access_token", accessToken, 1, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(user.getEmail() + "_refresh_token", refreshToken, 1, TimeUnit.DAYS);

        return  user;
    }
    public String getAccessToken(String email) {
        return redisTemplate.opsForValue().get(email + "_access_token");
    }
    public JwtDto login(String email, String password) throws BackingStoreException {
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user = userOptional.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        if(!passwordEncoder.matches(password, user.getPassword())) {
            throw new BackingStoreException("비밀번호 잘못됨");
        }


        String accessToken = redisTemplate.opsForValue().get(user.getEmail() + "_access_token");


        if (accessToken != null && jwtProvider.validateToken(accessToken)) {
            // 유효한 토큰이 있는 경우
            return JwtDto.builder()
                    .grantType("Bearer")
                    .accessToken(accessToken)
                    .refreshToken(redisTemplate.opsForValue().get(email + "_refresh_token")) // 기존 리프레시 토큰 반환
                    .build();
        }
        JwtDto accessTokenDto = jwtProvider.generateAccessToken(user);
        accessToken = accessTokenDto.accessToken();

        redisTemplate.opsForValue().set(user.getEmail() + "_access_token", accessToken, 1, TimeUnit.HOURS);

        String refreshToken = redisTemplate.opsForValue().get(email + "_refresh_token");
        if (refreshToken == null) {
            JwtDto refreshTokenDto = jwtProvider.generateRefreshToken(user);
            refreshToken = refreshTokenDto.refreshToken(); // 새 리프레시 토큰 추출
            redisTemplate.opsForValue().set(email + "_refresh_token", refreshToken, 1, TimeUnit.DAYS);
        }

        return JwtDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void logout(String email) {

    }

}
