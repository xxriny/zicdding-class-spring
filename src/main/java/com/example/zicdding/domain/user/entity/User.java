package com.example.zicdding.domain.user.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class User {
    final private Long id;
    final private String email;
    final private String nickname;
    final private String password;
    final private String phoneNumber;
    final private String roleType;
    final private String suspensionYn;
    final private String delYn;
    final private LocalDateTime createdDate;
    final private Long modUserId;
    final private LocalDateTime modDate;

    @Builder
    public User(Long id, String email, String nickname, String password, String phoneNumber, String roleType, String suspensionYn, String delYn, LocalDateTime createdDate, Long modUserId, LocalDateTime modDate) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.roleType = roleType != null ? roleType : "1"; // 기본값
        this.suspensionYn = suspensionYn != null ? suspensionYn : "N"; // 기본값
        this.delYn = delYn != null ? delYn : "N"; // 기본값
        this.createdDate = createdDate != null ? createdDate : LocalDateTime.now(); // 기본값
        this.modUserId = modUserId != null ? modUserId : 0; // 기본값, 필요에 따라 변경
        this.modDate = modDate != null ? modDate : LocalDateTime.now(); // 기본값
    }



}