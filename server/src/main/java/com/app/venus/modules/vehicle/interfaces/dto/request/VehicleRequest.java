package com.app.venus.modules.vehicle.interfaces.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VehicleRequest(
        @NotBlank(message = "Brand is required.") @Size(max = 80, message = "Brand must be at most 80 characters.") String brand,
        @NotBlank(message = "Model is required.") @Size(max = 80, message = "Model must be at most 80 characters.") String model,
        @NotNull(message = "Year is required.") @Min(value = 1990, message = "Year must be 1990 or later.") @Max(value = 2100, message = "Year must be 2100 or earlier.") Integer year,
        @NotBlank(message = "Connector type is required.") String connectorType,
        Boolean isDefault) {
}
