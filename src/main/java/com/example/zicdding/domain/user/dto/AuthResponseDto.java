package com.example.zicdding.domain.user.dto;

import com.example.zicdding.domain.user.entity.User;
import lombok.Builder;

@Builder
public record AuthResponseDto (
        Long id,
        String email,
        String nickname,
        String accessToken,
        String refreshToken
){
}
