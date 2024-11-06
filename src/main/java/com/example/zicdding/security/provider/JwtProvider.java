package com.example.zicdding.security.provider;

import com.example.zicdding.domain.user.dto.JwtDto;
import com.example.zicdding.domain.user.entity.User;
import com.example.zicdding.domain.user.service.CustomUserDetailService;

import com.example.zicdding.global.common.enums.UserRole;
import com.example.zicdding.global.exception.CustomException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
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


    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    //인증정보 가져오기
    public Claims getClaims(String token){
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
    public Authentication getAuthentication(String token) {
        String email =getEmailFromToken(token);
        UserDetails userDetails = customUserDetailService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
    //만료 시간 가져오기
    public Date getExpirationDateFromToken(String token) {
        return getClaims(token).getExpiration();
    }
    //accesstoken
    public JwtDto generateAccessToken(User user) {
        return generateToken(user, accessTokenExpireTime, false);
    }
    //refreshtoken
    public JwtDto generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpireTime, true);
    }
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }
    //토큰 생성
    private JwtDto generateToken(User user, long expireTime,  boolean isRefreshToken) {
        Date expirationDate = new Date(System.currentTimeMillis() + expireTime * 1000); // 밀리초로 변환
        System.out.println(expirationDate);
        Claims claims = Jwts.claims().setSubject(user.getEmail());
        claims.put("role", user.getRoleType());

          String token = Jwts.builder()
                  .setClaims(claims)
                  .setExpiration(expirationDate)
                  .signWith(SignatureAlgorithm.HS256, key)
                  .compact();

        return JwtDto.builder()
                .grantType("Bearer")
                .accessToken(isRefreshToken ? null : token) // 리프레시 토큰이면 액세스 토큰은 null
                .refreshToken(isRefreshToken ? token : null)
                .build();
    }



    public Boolean validateToken(String token) {
        try{
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        }catch (MalformedJwtException e){
            log.error("Invalid JWT token : {}", e.getMessage());
        }catch(ExpiredJwtException e){
            log.error("Expired JWT token : {}", e.getMessage());
        }catch (SecurityException e){
            log.error("Invalid JWT token : {}", e.getMessage());
        }catch (IllegalArgumentException e){
            log.error("JWT claims string is empty : {}", e.getMessage());
        }catch (UnsupportedJwtException e){
            log.error("Unsupported JWT token : {}", e.getMessage());
        }
        return false;
        }
}
