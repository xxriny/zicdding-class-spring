package com.example.zicdding.security.filter;

import com.example.zicdding.domain.user.entity.User;
import com.example.zicdding.domain.user.service.CustomUserDetailService;

import com.example.zicdding.domain.user.service.UserService;
import com.example.zicdding.global.common.enums.ErrorCodeEnum;
import com.example.zicdding.global.exception.CustomException;

import com.example.zicdding.global.util.JwtUtil;
import com.example.zicdding.security.CustomUserDetail;
import com.example.zicdding.security.provider.JwtProvider;

import io.jsonwebtoken.Jwt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final CustomUserDetailService customUserDetailService;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String accessToken = resolveAccessToken(request);
        System.out.println("Filter start-----------------------------------------------------------------------------" + accessToken);
            if ( accessToken != null && jwtProvider.validateToken(accessToken)) {
//                Authentication authentication = jwtProvider.getAuthentication(accessToken);
//                SecurityContextHolder.getContext().setAuthentication(authentication);
                String username = jwtProvider.getEmailFromToken(accessToken);

                CustomUserDetail userDetail = (CustomUserDetail) customUserDetailService.loadUserByUsername(username);
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetail, null, userDetail.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            chain.doFilter(request, response);
    }    /**
     * Request Header에서 Access Token을 쿠키로 조회
     */
    private String resolveAccessToken(HttpServletRequest request) {
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


    /**
     * Request Header에서 Refresh Token을 쿠키로 조회
     */
    private String resolveRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}