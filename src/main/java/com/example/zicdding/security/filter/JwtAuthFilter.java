package com.example.zicdding.security.filter;

import ApiExceptionEnum.ACCESS_DENIED;
import com.example.zicdding.global.common.enums.ErrorCodeEnum;
import com.example.zicdding.global.exception.CustomException;
import com.example.zicdding.security.handler.CustomAccessDeniedHandler;
import com.example.zicdding.security.provider.JwtProvider;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String accessToken = resolveAccessToken(request);
        System.out.println(accessToken + "---------------------------------dofilter 시작");
        if (jwtProvider.validateToken(accessToken)) {
            Authentication authentication = jwtProvider.getAuthentication(accessToken);
            if (authentication != null) {
                System.out.println("SecurityContext 설정 시 이메일: " + authentication.getName());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                System.out.println("Authentication 객체가 null입니다.");
            }
        }      chain.doFilter(request, response);

    }
    public String resolveAccessToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    System.out.println(cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        throw new CustomException(ErrorCodeEnum.ACCESS_DENIED);
    }
}