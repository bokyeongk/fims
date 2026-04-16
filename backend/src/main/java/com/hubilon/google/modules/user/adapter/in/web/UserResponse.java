package com.hubilon.google.modules.user.adapter.in.web;

import com.hubilon.google.modules.user.domain.model.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String name,
        String role,
        String status,
        LocalDateTime createDate,
        LocalDateTime modifyDate
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreateDate(),
                user.getModifyDate()
        );
    }
}
