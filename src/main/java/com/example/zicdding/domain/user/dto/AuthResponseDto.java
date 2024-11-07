package com.example.zicdding.domain.user.dto;

import lombok.Builder;

@Builder
public record AuthResponseDto (
        String email,
        String accessToken,
        String refreshToken
){}
