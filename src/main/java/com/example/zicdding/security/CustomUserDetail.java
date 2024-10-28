package com.example.zicdding.security;

import com.example.zicdding.domain.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public record CustomUserDetail(User user) implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_" + user.getRoleType().toString());
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return user.getNickname();
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
