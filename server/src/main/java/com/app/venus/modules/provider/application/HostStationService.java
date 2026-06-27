package com.app.venus.modules.provider.application;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.venus.modules.provider.domain.Station;
import com.app.venus.modules.provider.infrastructure.StationRepository;
import com.app.venus.modules.provider.interfaces.dto.request.HostStationRequest;
import com.app.venus.modules.user.application.DemoCurrentUserService;
import com.app.venus.modules.user.domain.User;
import com.app.venus.shared.domain.Amenity;
import com.app.venus.shared.domain.ConnectorType;
import com.app.venus.shared.domain.PublicIdGenerator;
import com.app.venus.shared.exception.NotFoundException;
import com.app.venus.shared.exception.UnprocessableEntityException;

@Service
public class HostStationService {
    private final StationRepository stationRepository;
    private final DemoCurrentUserService currentUserService;
    private final PublicIdGenerator publicIdGenerator;

    public HostStationService(
            StationRepository stationRepository,
            DemoCurrentUserService currentUserService,
            PublicIdGenerator publicIdGenerator) {
        this.stationRepository = stationRepository;
        this.currentUserService = currentUserService;
        this.publicIdGenerator = publicIdGenerator;
    }

    @Transactional(readOnly = true)
    public Station getCurrentProviderStation() {
        return stationRepository.findByProviderId(currentUserService.currentProviderId())
                .orElseThrow(() -> new NotFoundException("Station not found."));
    }

    @Transactional
    public Station upsertCurrentProviderStation(HostStationRequest request) {
        User provider = currentUserService.currentProvider();
        Set<ConnectorType> connectorTypes = parseConnectorTypes(request.connectorTypes());
        Set<Amenity> amenities = parseAmenities(request.amenities());
        List<String> photoUrls = request.photoUrls() == null ? List.of() : request.photoUrls();
        boolean available = request.isAvailable() == null || request.isAvailable();

        Station station = stationRepository.findByProviderId(provider.getId())
                .orElseGet(() -> new Station(
                        publicIdGenerator.nextId("pvd"),
                        provider,
                        request.name(),
                        request.address(),
                        request.lat(),
                        request.lng(),
                        request.pricePerHour(),
                        connectorTypes,
                        amenities,
                        photoUrls,
                        available));

        station.update(
                request.name(),
                request.address(),
                request.lat(),
                request.lng(),
                request.pricePerHour(),
                connectorTypes,
                amenities,
                photoUrls,
                available);

        return stationRepository.saveAndFlush(station);
    }

    private Set<ConnectorType> parseConnectorTypes(List<String> values) {
        Set<ConnectorType> connectorTypes = new LinkedHashSet<>();
        for (String value : values) {
            try {
                connectorTypes.add(ConnectorType.fromValue(value));
            } catch (IllegalArgumentException exception) {
                throw new UnprocessableEntityException("Connector type must be one of: Type 1, Type 2, CCS, CHAdeMO.");
            }
        }
        return connectorTypes;
    }

    private Set<Amenity> parseAmenities(List<String> values) {
        if (values == null) {
            return Set.of();
        }
        Set<Amenity> amenities = new LinkedHashSet<>();
        for (String value : values) {
            try {
                amenities.add(Amenity.fromValue(value));
            } catch (IllegalArgumentException exception) {
                throw new UnprocessableEntityException("Amenity must be one of: Coffee, WiFi, Air Conditioning, Restroom, Parking, Covered, Security, Snacks.");
            }
        }
        return amenities;
    }
}
