package com.example.zicdding.security.filter;

import com.example.zicdding.domain.user.service.CustomUserDetailService;
import com.example.zicdding.domain.user.service.UserReadService;
import com.example.zicdding.security.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.GenericFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final UserReadService userReadService;
    private final FilterChainProxy filterChainProxy;
    private final CustomUserDetailService customUserDetailService;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        final String token = request.getHeader("Authorization");



        if(token != null && !token.isEmpty()){
           String email = jwtProvider.getEmail(token);
            UserDetails userDetails = customUserDetailService.loadUserByUsername(email);
            if(userDetails  != null  && SecurityContextHolder.getContext().getAuthentication() == null){
                //details, password, role 정보 기반 접근 권한 갖고 있는 토큰 생성
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }
    /**
     * Request Header에서 토큰 조회 및 Bearer 문자열 제거 후 반환하는 메소드
     * @param request HttpServletRequest
     * @return 추출된 토큰 정보 반환 (토큰 정보가 없을 경우 null 반환)
     */
    private String resolveToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        // Token 정보 존재 여부 및 Bearer 토큰인지 확인
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        return null;
    }
}
