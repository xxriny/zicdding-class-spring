package com.example.zicdding.global.common.enums;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum UserRole {
    MEMBER("ROLE_MEMBER"),
    ADMIN("ROLE_ADMIN");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public SimpleGrantedAuthority toAuthority() {
        return new SimpleGrantedAuthority(role);
    }
}
