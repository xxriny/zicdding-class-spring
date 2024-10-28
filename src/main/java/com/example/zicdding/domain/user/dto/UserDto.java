package com.example.zicdding.domain.user.dto;

public record UserDto(Long id, String email, String nickname, String accessToken, String refreshToken, String phoneNumber) {}
