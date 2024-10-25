package com.example.zicdding.domain.user.service;

import com.example.zicdding.domain.user.dto.UserDto;
import com.example.zicdding.domain.user.dto.UserSaveDto;
import com.example.zicdding.domain.user.entity.User;
import com.example.zicdding.domain.user.repository.UserRepository;
import com.example.zicdding.util.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {
    final private UserRepository userRepository;
    private final PasswordService passwordService;


    public User signIn(UserSaveDto userSaveDto) {
        String encryptPassword = passwordService.encryptPassword(userSaveDto.password());
        var user = User.builder()
                .email(userSaveDto.email())
                .nickname(userSaveDto.nickname())
                .password(encryptPassword)
                .phoneNumber(userSaveDto.phoneNumber())
                .build();
        return userRepository.save(user);
    }


}
