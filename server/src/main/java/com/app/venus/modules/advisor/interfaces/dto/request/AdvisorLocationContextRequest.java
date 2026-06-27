package com.app.venus.modules.advisor.interfaces.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record AdvisorLocationContextRequest(
        @Size(max = 80) String district,
        @Size(max = 80) String city,
        @Size(max = 240) String address,
        @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal lat,
        @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal lng,
        @Min(0) Integer nearbyChargerCount,
        @Size(max = 12) List<@Size(max = 60) String> siteTypeSignals,
        @Size(max = 40) String longStayParkingPotential,
        @Size(max = 60) String demandPotentialLabel,
        @Size(max = 80) String curatedLocationId) {
}
