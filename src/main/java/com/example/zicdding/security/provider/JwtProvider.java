package com.example.zicdding.security.provider;

import com.example.zicdding.domain.user.service.CustomUserDetailService;
import com.example.zicdding.global.common.enums.ErrorCodeEnum;
import com.example.zicdding.global.exception.CustomException;
import com.example.zicdding.global.exception.dto.ExceptionDto;
import com.example.zicdding.global.util.RedisUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider  {

    private final CustomUserDetailService customUserDetailService;

    private Key key;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpireTime;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpireTime;

    private final RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // Claims 가져오기
    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public Authentication getAuthentication(String token) {
        String email = getClaims(token).getSubject();
        UserDetails userDetails = customUserDetailService.loadUserByUsername(email);

        if (userDetails == null) {
            throw new CustomException(ErrorCodeEnum.USER_NOT_FOUND);
        }
        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }

    public String generateAccessToken(Authentication authentication) {
        Claims claims= Jwts.claims().setSubject(authentication.getName());
        long now = (new Date()).getTime();

        // Access Token 생성
        Date accessTokenExpiresIn = new Date(now + accessTokenExpireTime);
        System.out.println(accessTokenExpiresIn + "만료시간");

        System.out.println("claims" + claims.getSubject());
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(accessTokenExpiresIn)
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();

    }
    // AccessToken 생성
    public String generateRefreshToken(Authentication authentication) {
        Claims claims= Jwts.claims().setSubject(authentication.getName());
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + refreshTokenExpireTime);
        String refreshToken = Jwts.builder().setClaims(claims).setExpiration(expireDate).signWith(SignatureAlgorithm.HS512, key).compact();
        redisTemplate.opsForValue().set(authentication.getName(), refreshToken);
        return refreshToken;
    }

    // Redis에서 RefreshToken 가져오기
    public String getRefreshTokenFromRedis(String email) {
        return redisTemplate.opsForValue().get(email);
    }

    // AccessToken 유효성 검사
    public boolean validateAccessToken(String accessToken) {
        String tokenStatus = redisTemplate.opsForValue().get(accessToken);
        if ("accessToken".equals(tokenStatus)) {
            log.error("This token is blacklisted.");
            return false;
        }else{
            try {
                Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
                Date expiration = claims.getExpiration();
                Date now = new Date(System.currentTimeMillis() + accessTokenExpireTime);
                return !expiration.before(now);
            } catch (ExpiredJwtException e) {
                log.error("Expired JWT token: {}", e.getMessage());
                return false;
            } catch (MalformedJwtException | UnsupportedJwtException | SecurityException | IllegalArgumentException e) {
                log.error("Invalid JWT token: {}", e.getMessage());
                return false;
            }
        }

    }

    // 일반 토큰 유효성 검사 (AccessToken, RefreshToken 모두 가능)
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException | UnsupportedJwtException | SecurityException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}
