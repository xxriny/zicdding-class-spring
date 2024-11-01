package com.example.zicdding.domain.user.dto;

import lombok.Builder;

@Builder
public record AuthResponseDto (
        Long id,
        String email,
        String accessToken,
        String refreshToken
){}
