package com.example.zicdding.domain.user.entity;

import com.example.zicdding.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;


@Getter
@Setter
@Table(name = "TB_USER")
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // final 제거

    private String email;
    private String password;
    private String nickname;
    @Column(name = "phone_num")
    private String phoneNumber;
    private String roleType;
    private String suspensionYn;
    private String delYn;
    private LocalDateTime createdDate;
    private Long modUserId;
    private LocalDateTime modDate;
    private String refreshToken;
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
