package com.example.zicdding.domain.user.dto;

import lombok.Builder;

@Builder
public record AuthResponseDto (
        UserDto user,
        JwtDto jwt
){
}
