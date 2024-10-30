package com.example.zicdding.security.provider;

import com.example.zicdding.domain.user.dto.JwtDto;
import com.example.zicdding.domain.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider  {
    private Key key;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpireTime;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpireTime;


    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    private Claims getAuthentication(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Date getExpirationDateFromToken(String token) {
        return getAuthentication(token).getExpiration();
    }

    public JwtDto generateAccessToken(User user) {
        return generateToken(user, accessTokenExpireTime, false);
    }

    public JwtDto generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpireTime, true);
    }

    private JwtDto generateToken(User user, long expireTime,  boolean isRefreshToken) {
        Date expirationDate = new Date(System.currentTimeMillis() + expireTime * 1000); // 밀리초로 변환
        Claims claims = Jwts.claims().setSubject(user.getEmail());
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

    public String getEmail(final String token) {
        return getAuthentication(token).getSubject();
    }

    public Boolean validateToken(String token) {
        try{
            Date expirationDate = getExpirationDateFromToken(token);
            return !expirationDate.before(new Date());
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
