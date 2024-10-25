package com.example.zicdding.application.controller;

import com.example.zicdding.domain.user.dto.UserDto;
import com.example.zicdding.domain.user.dto.UserSaveDto;
import com.example.zicdding.domain.user.service.UserReadService;
import com.example.zicdding.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    final UserService userService;
    private final UserReadService userReadService;

    @PostMapping("/signIn")
    public UserDto signIn(@RequestBody UserSaveDto userSaveDtoDto) {
        var user = userService.signIn(userSaveDtoDto);
        return userReadService.toDto(user);
    }
    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }
}
