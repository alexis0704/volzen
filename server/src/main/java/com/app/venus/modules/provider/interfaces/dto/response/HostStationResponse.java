package com.app.venus.modules.provider.interfaces.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.app.venus.modules.provider.domain.Station;
import com.app.venus.shared.domain.Amenity;
import com.app.venus.shared.domain.ConnectorType;

public record HostStationResponse(
        String id,
        String name,
        String address,
        BigDecimal lat,
        BigDecimal lng,
        int pricePerHour,
        List<String> connectorTypes,
        List<String> amenities,
        List<String> photoUrls,
        boolean isAvailable,
        String status) {

    public static HostStationResponse from(Station station) {
        return new HostStationResponse(
                station.getId(),
                station.getName(),
                station.getAddress(),
                station.getLat(),
                station.getLng(),
                station.getPricePerHour(),
                station.getConnectorTypes().stream().map(ConnectorType::getValue).sorted().toList(),
                station.getAmenities().stream().map(Amenity::getValue).sorted().toList(),
                station.getPhotoUrls(),
                station.isAvailable(),
                station.isAvailable() ? "Active" : "Inactive");
    }
}
