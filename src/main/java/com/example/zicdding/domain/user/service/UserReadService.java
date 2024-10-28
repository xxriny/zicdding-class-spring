package com.example.zicdding.domain.user.service;

import com.example.zicdding.domain.user.dto.UserDto;
import com.example.zicdding.domain.user.entity.User;
import com.example.zicdding.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserReadService {

    final private UserRepository userRepository;

    public UserDto toDto(User user, String accessToken) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                accessToken,
                user.getRefreshToken(),
                user.getPhoneNumber()
        ); }
    /**
     사용자 idx를 이용하여 사용자 정보를 조회함
     @param userId
     @return 사용자 정보
      * */
//    public UserDto getUserId(Long userId) {
//        var user = userRepository.findById(userId).orElseThrow();
//        return toDto(user);
//    }
    /**
        사용자 id를 이용하여 사용자 정보를 조회함
     @param email
     @return 사용자 정보
     * */
//    public UserDto getUserEmail(String email){
//        var user =  userRepository.findByEmail(email).orElseThrow();
//        return toDto(user);
//    }
}
