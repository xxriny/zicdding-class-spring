package com.example.zicdding.domain.user.service;

import com.example.zicdding.config.exception.enums.ApiExceptionEnum;
import com.example.zicdding.domain.user.entity.User;
import com.example.zicdding.domain.user.repository.UserRepository;

import com.example.zicdding.security.CustomUserDetail;
import com.example.zicdding.security.handler.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApplicationException(ApiExceptionEnum.USER_NOT_FOUND)); // ApiExceptionEnum 사용
        return new CustomUserDetail(user);
    }
}
