package com.example.zicdding.user;


import com.example.zicdding.domain.user.entity.User;
import com.example.zicdding.domain.user.repository.UserRepository;
import com.example.zicdding.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class testCheckEmail {
    JdbcTemplate namedParameterJdbcTemplate;
    @Mock
    UserRepository userRepository;
    @InjectMocks
    UserService userService;
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Mock 객체 초기화
    }


    @Test
    public void testFindByEmail() {
        String email = "xxrin01299@gmail.com";

   
        // 이메일이 존재하는지 확인
        Optional<User> user = userRepository.findByEmail(email);
        System.out.println("Retrieved User: " + user.orElse(null));
    }
}
