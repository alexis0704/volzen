package com.app.venus.modules.advisor.interfaces.dto.request;

import com.app.venus.modules.advisor.domain.AdvisorProvider;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdvisorChatRequest(
        @NotBlank @Size(max = 1200) String message,
        @Size(max = 80) String conversationId,
        @Valid AdvisorLocationContextRequest locationContext,
        AdvisorProvider preferredProvider) {
}
