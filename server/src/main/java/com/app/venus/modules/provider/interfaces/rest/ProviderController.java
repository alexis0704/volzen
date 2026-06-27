package com.app.venus.modules.provider.interfaces.rest;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.venus.modules.provider.application.ProviderDiscoveryService;
import com.app.venus.modules.provider.interfaces.dto.response.ProviderResponses.AvailabilityResponse;
import com.app.venus.modules.provider.interfaces.dto.response.ProviderResponses.ProviderDetailResponse;
import com.app.venus.modules.provider.interfaces.dto.response.ProviderResponses.ProvidersResponse;
import com.app.venus.shared.web.ApiPaths;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@RestController
@Validated
@RequestMapping(ApiPaths.API_V1 + "/providers")
public class ProviderController {
    private final ProviderDiscoveryService providerDiscoveryService;

    public ProviderController(ProviderDiscoveryService providerDiscoveryService) {
        this.providerDiscoveryService = providerDiscoveryService;
    }

    @GetMapping
    public ProvidersResponse searchProviders(
            @RequestParam @NotNull @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") BigDecimal lat,
            @RequestParam @NotNull @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") BigDecimal lng,
            @RequestParam(required = false) @DecimalMin(value = "0.1") Double radiusKm,
            @RequestParam(required = false) String connectorType,
            @RequestParam(required = false) @Min(0) Integer maxPricePerHour,
            @RequestParam(required = false) @Min(0) Integer limit,
            @RequestParam(required = false) @Min(0) Integer offset) {
        return ProvidersResponse.from(providerDiscoveryService.searchProviders(
                lat,
                lng,
                radiusKm,
                connectorType,
                maxPricePerHour,
                limit,
                offset));
    }

    @GetMapping("/{providerId}")
    public ProviderDetailResponse getProvider(@PathVariable String providerId) {
        return ProviderDetailResponse.from(providerDiscoveryService.getProviderDetail(providerId));
    }

    @GetMapping("/{providerId}/availability")
    public AvailabilityResponse getProviderAvailability(
            @PathVariable String providerId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return AvailabilityResponse.from(providerDiscoveryService.getAvailability(providerId, date));
    }
}
