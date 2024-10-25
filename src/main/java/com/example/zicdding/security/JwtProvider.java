package com.example.zicdding.security;

import com.example.zicdding.domain.user.dto.JwtDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;


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

    public String getUsernameFromToken(final String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        if(validateToken(token)) {
            return null;
        }

        final Claims claims =  getAuthentication(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAuthentication(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJwt(token)
                .getBody();
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public JwtDto generateAccessToken(Long id){
        return generateToken(id, accessTokenExpireTime);
    }
    public JwtDto generateRefreshToken(Long  id ){
        return generateToken(id, refreshTokenExpireTime);
    }

    private JwtDto generateToken(Long userId, long expireTime) {
        Date expirationDate = new Date(System.currentTimeMillis() + expireTime * 1000); // 밀리초로 변환

          String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
        return JwtDto.builder()
                .grantType("Bearer")
                .accessToken(token)
                .build();
    }



    // 사용자 속성 정보 조회


    public Boolean validateToken(String token) {
        try{
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
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
