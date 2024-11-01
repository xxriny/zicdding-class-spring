package com.example.zicdding.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    final private Long id;
    final private String email;
    final private String password;
    final private String nickname;
    final private String phoneNumber;
    final private String roleType;
    final private String suspensionYn;
    final private String delYn;
    final private LocalDateTime createdDate;
    final private Long modUserId;
    final private LocalDateTime modDate;

    final private String refreshToken;
    @Builder(toBuilder = true)
    public User(Long id, String email,String password, String nickname,  String phoneNumber, String roleType, String suspensionYn, String delYn, LocalDateTime createdDate, Long modUserId, LocalDateTime modDate,  String refreshToken) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.roleType = roleType != null ? roleType : "1"; // 기본값
        this.suspensionYn = suspensionYn != null ? suspensionYn : "N"; // 기본값
        this.delYn = delYn != null ? delYn : "N"; // 기본값
        this.createdDate = createdDate != null ? createdDate : LocalDateTime.now(); // 기본값
        this.modUserId = modUserId != null ? modUserId : 0; // 기본값, 필요에 따라 변경
        this.modDate = modDate != null ? modDate : LocalDateTime.now(); // 기본값
        this.refreshToken = refreshToken;
    }



}
