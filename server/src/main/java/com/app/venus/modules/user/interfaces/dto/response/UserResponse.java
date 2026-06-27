package com.app.venus.modules.user.interfaces.dto.response;

import java.time.Instant;

import com.app.venus.modules.user.domain.User;

public record UserResponse(
        String id,
        String fullName,
        String email,
        String role,
        String avatarUrl,
        Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().getValue(),
                user.getAvatarUrl(),
                user.getCreatedInstant());
    }
}
