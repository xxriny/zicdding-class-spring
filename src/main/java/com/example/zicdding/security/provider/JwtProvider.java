package com.example.zicdding.security.provider;

import com.example.zicdding.domain.user.service.CustomUserDetailService;

import com.example.zicdding.global.util.RedisUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisUtil redisUtil;
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

    //인증정보 가져오기
    public Claims getClaims(String token){
            return Jwts.parserBuilder().setSigningKey(key).setAllowedClockSkewSeconds(30).build().parseClaimsJws(token).getBody();
    }
    //토큰으로부터 Claims 만들고 user객체와 authentication 리턴
    public Authentication getAuthentication(String token) {
        String email =getEmailFromToken(token);
        UserDetails userDetails = customUserDetailService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
    //만료 시간 가져오기
    public Long getExpirationDateFromToken(String token) {
        Date expiration = getClaims(token).getExpiration();
        long now = new Date().getTime();
        return expiration.getTime() - now;
    }
    //accesstoken
    public String generateAccessToken(Authentication authentication) {
        Claims claims= Jwts.claims().setSubject(authentication.getName());
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + accessTokenExpireTime);
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();

    }
    //refreshtoken
    public void generateRefreshToken(Authentication authentication) {
        Claims claims= Jwts.claims().setSubject(authentication.getName());
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + refreshTokenExpireTime);
        String refreshToken = Jwts.builder().setClaims(claims).setExpiration(expireDate).signWith(SignatureAlgorithm.HS512, key).compact();
        redisUtil.save(authentication.getName(), refreshToken);
        System.out.println("완료");
    }

    public String getRefreshTokenFromRedis(String email) {
        return redisTemplate.opsForValue().get(email);
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }
//    //토큰 생성
//    private String generateToken(User user, long expireTime,  boolean isRefreshToken) {
//        Date expirationDate = new Date(System.currentTimeMillis() + expireTime * 1000); // 밀리초로 변환
//        System.out.println(expirationDate);
//        Claims claims = Jwts.claims().setSubject(user.getEmail());
//        claims.put("role", user.getRoleType());
//
//          String token = Jwts.builder()
//                  .setClaims(claims)
//                  .setExpiration(expirationDate)
//                  .signWith(SignatureAlgorithm.HS256, key)
//                  .compact();
//
//        return JwtDto.builder()
//                .grantType("Bearer")
//                .accessToken(isRefreshToken ? null : token) // 리프레시 토큰이면 액세스 토큰은 null
//                .refreshToken(isRefreshToken ? token : null)
//                .build();
//    }



    public Boolean validateToken(String token) {
        try{
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
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
