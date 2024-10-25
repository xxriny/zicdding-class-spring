package com.example.zicdding.security.filter;

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

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        final String token = request.getHeader("Authorization");

        String userId = null;

        if(token != null && !token.isEmpty()){
            String jwtToken = token.substring(7);
            userId = jwtProvider.getUsernameFromToken(jwtToken);
        }

        if(userId != null && !userId.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null){
            SecurityContextHolder.getContext().setAuthentication(getUserAuth(Long.valueOf(userId)));
        }
        chain.doFilter(request, response);
    }
    private UsernamePasswordAuthenticationToken getUserAuth(Long id) {
        var userInfo = userReadService.getUserId(id);

        return new UsernamePasswordAuthenticationToken(userInfo.email(),
                userInfo.password(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
