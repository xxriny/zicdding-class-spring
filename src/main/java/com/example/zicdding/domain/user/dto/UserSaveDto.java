package com.example.zicdding.domain.user.dto;


public record UserSaveDto(
                          Long id,
                          String email,
                          String nickname,
                          String password,
                          String phoneNumber
){
}
