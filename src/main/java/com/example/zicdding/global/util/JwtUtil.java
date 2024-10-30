package com.example.zicdding.global.util;

import com.example.zicdding.security.CustomUserDetail;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.rmi.AccessException;


public class JwtUtil {
    public Long getLoginId(final Authentication authentication) throws AccessException{
        checkAuth(authentication);
        return Long.parseLong(authentication.getPrincipal().toString());}

    private void checkAuth(final Authentication authentication) {
        if(authentication == null) {
            throw new SecurityException("Authentication is null");
        }
    }
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return ((CustomUserDetail) authentication.getPrincipal()).getEmail(); // 또는 getEmail()을 구현하여 이메일 가져오기
        }
        throw new SecurityException("사용자가 인증되지 않았습니다.");
    }
}
