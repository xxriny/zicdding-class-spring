package com.example.zicdding.domain.user.service;

import com.example.zicdding.domain.user.dto.UserInfoDto;
import com.example.zicdding.domain.user.entity.User;
import com.example.zicdding.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserReadService {

    final private UserRepository userRepository;
    /**
     사용자 인증정보 사용하여 사용자의 정보 조회
     @param email
     @return 사용자 정보
      * */
    public UserInfoDto getUserInfo(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return new UserInfoDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getPhoneNumber()
        ); }

}
