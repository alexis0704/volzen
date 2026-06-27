package com.app.venus.modules.order.interfaces.dto.request;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotBlank String providerId,
        @NotBlank String vehicleId,
        @NotNull OffsetDateTime startTime,
        @NotNull OffsetDateTime endTime) {
}
