package com.example.zicdding.domain.user.dto;

import lombok.Builder;

@Builder
public record JwtDto(
        String grantType,
        String accessToken,
        String refreshToken
){}
