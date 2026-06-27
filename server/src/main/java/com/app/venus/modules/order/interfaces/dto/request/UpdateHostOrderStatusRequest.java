package com.app.venus.modules.order.interfaces.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateHostOrderStatusRequest(@NotBlank(message = "Status is required.") String status) {
}
