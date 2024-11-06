package com.example.zicdding.domain.user.service;

import com.example.zicdding.domain.user.dto.AuthResponseDto;
import com.example.zicdding.global.common.enums.ErrorCodeEnum;
import com.example.zicdding.domain.user.entity.User;
import com.example.zicdding.domain.user.repository.UserRepository;

import com.example.zicdding.global.exception.CustomException;
import com.example.zicdding.security.CustomUserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       return   userRepository.findByEmail(username)
                .map(CustomUserDetail::new)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }
}