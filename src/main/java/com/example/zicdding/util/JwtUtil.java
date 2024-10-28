package com.example.zicdding.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;

import java.rmi.AccessException;

@UtilityClass
public class JwtUtil {
    public Long getLoginId(final Authentication authentication) throws AccessException{
        checkAuth(authentication);
        return Long.parseLong(authentication.getPrincipal().toString());}

    private void checkAuth(final Authentication authentication) {
        if(authentication == null) {
            throw new SecurityException("Authentication is null");
        }
    }
}
