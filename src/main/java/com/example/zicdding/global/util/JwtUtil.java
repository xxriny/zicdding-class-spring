package com.example.zicdding.global.util;


import com.example.zicdding.domain.user.repository.UserRepository;
import com.example.zicdding.security.CustomUserDetail;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    public Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail) {
            CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();
            return userDetail.getUser().getId(); // CustomUserDetail에서 user_id를 가져온다고 가정
        }
        return null;
    }
}