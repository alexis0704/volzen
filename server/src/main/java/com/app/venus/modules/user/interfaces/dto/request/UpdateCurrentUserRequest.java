package com.app.venus.modules.user.interfaces.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateCurrentUserRequest(
        @Size(max = 120, message = "Full name must be at most 120 characters.") String fullName,
        @Size(max = 500, message = "Avatar URL must be at most 500 characters.") String avatarUrl) {
}
