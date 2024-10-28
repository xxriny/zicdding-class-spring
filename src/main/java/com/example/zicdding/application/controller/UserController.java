package com.example.zicdding.application.controller;

import com.example.zicdding.domain.user.dto.JwtDto;
import com.example.zicdding.domain.user.dto.UserDto;
import com.example.zicdding.domain.user.dto.UserSaveDto;
import com.example.zicdding.domain.user.service.UserReadService;
import com.example.zicdding.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.prefs.BackingStoreException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    final UserService userService;
    private final UserReadService userReadService;

    @PostMapping("/signUp")
    public UserDto signUp(@RequestBody UserSaveDto userSaveDtoDto) {
        var user = userService.signUp(userSaveDtoDto);
        String accessToken = userService.getAccessToken(user.getEmail());
        return userReadService.toDto(user, accessToken);
    }
    @PostMapping("/login")
    public JwtDto signIn(@RequestParam String email, @RequestParam String password) throws BackingStoreException {
       return userService.login(email,password);
    }
}
