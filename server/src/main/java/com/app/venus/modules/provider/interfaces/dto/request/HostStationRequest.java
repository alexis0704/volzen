package com.app.venus.modules.provider.interfaces.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HostStationRequest(
        @NotBlank(message = "Station name is required.") @Size(max = 120, message = "Station name must be at most 120 characters.") String name,
        @NotBlank(message = "Address is required.") @Size(max = 255, message = "Address must be at most 255 characters.") String address,
        @NotNull(message = "Latitude is required.") @DecimalMin(value = "-90.0", message = "Latitude must be at least -90.") @DecimalMax(value = "90.0", message = "Latitude must be at most 90.") BigDecimal lat,
        @NotNull(message = "Longitude is required.") @DecimalMin(value = "-180.0", message = "Longitude must be at least -180.") @DecimalMax(value = "180.0", message = "Longitude must be at most 180.") BigDecimal lng,
        @NotNull(message = "Price per hour is required.") @Min(value = 1, message = "Price per hour must be positive.") Integer pricePerHour,
        @NotEmpty(message = "At least one connector type is required.") List<@NotBlank(message = "Connector type is required.") String> connectorTypes,
        List<@NotBlank(message = "Amenity is required.") String> amenities,
        List<@Size(max = 500, message = "Photo URL must be at most 500 characters.") String> photoUrls,
        Boolean isAvailable) {
}
