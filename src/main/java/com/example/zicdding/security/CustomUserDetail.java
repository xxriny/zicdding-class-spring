package com.example.zicdding.security;

import com.example.zicdding.domain.user.entity.User;
import com.example.zicdding.global.common.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


public record CustomUserDetail(User user)  implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        UserRole role;

        if ("1".equals(user.getRoleType())) {
            role = UserRole.ADMIN;
        } else if ("2".equals(user.getRoleType())) {
            role = UserRole.MEMBER;
        } else {
            throw new IllegalArgumentException("Invalid role type");
        }
        return List.of(role.toAuthority());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }
    public User getUser() {
        return user;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // JWT 인증방식이므로 접근 가능하도록 설정
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // JWT 인증방식이므로 접근 가능하도록 설정
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // JWT 인증방식이므로 접근 가능하도록 설정
    }

    @Override
    public boolean isEnabled() {
        return true; // JWT 인증방식의므로 접근 가능하도록 설정
    }


}
