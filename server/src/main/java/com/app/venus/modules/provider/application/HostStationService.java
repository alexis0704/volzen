package com.app.venus.modules.provider.application;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.venus.modules.provider.domain.Station;
import com.app.venus.modules.provider.infrastructure.StationRepository;
import com.app.venus.modules.provider.interfaces.dto.request.HostStationRequest;
import com.app.venus.modules.provider.interfaces.dto.response.HostStationResponse;
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
    public HostStationResponse getCurrentProviderStationResponse() {
        Station station = stationRepository.findFirstByProviderIdOrderByIdAsc(currentUserService.currentProviderId())
                .orElseThrow(() -> new NotFoundException("Station not found."));
        return HostStationResponse.from(station);
    }

    @Transactional(readOnly = true)
    public List<HostStationResponse> listCurrentProviderStationResponses() {
        return stationRepository.findByProviderIdOrderByIdAsc(currentUserService.currentProviderId())
                .stream()
                .map(HostStationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Station getCurrentProviderStation() {
        return stationRepository.findFirstByProviderIdOrderByIdAsc(currentUserService.currentProviderId())
                .orElseThrow(() -> new NotFoundException("Station not found."));
    }

    @Transactional
    public HostStationResponse upsertCurrentProviderStationResponse(HostStationRequest request) {
        HostStationRequest actualRequest = request;
        User provider = currentUserService.currentProvider();
        Set<ConnectorType> connectorTypes = parseConnectorTypes(actualRequest.connectorTypes());
        Set<Amenity> amenities = parseAmenities(actualRequest.amenities());
        List<String> photoUrls = actualRequest.photoUrls() == null ? List.of() : actualRequest.photoUrls();
        boolean available = actualRequest.isAvailable() == null || actualRequest.isAvailable();

        Station station = stationRepository.findFirstByProviderIdOrderByIdAsc(provider.getId())
                .orElseGet(() -> new Station(
                        publicIdGenerator.nextId("pvd"),
                        provider,
                        actualRequest.name(),
                        actualRequest.address(),
                        actualRequest.lat(),
                        actualRequest.lng(),
                        actualRequest.pricePerHour(),
                        connectorTypes,
                        amenities,
                        photoUrls,
                        available));

        station.update(
                actualRequest.name(),
                actualRequest.address(),
                actualRequest.lat(),
                actualRequest.lng(),
                actualRequest.pricePerHour(),
                connectorTypes,
                amenities,
                photoUrls,
                available);

        station = stationRepository.saveAndFlush(station);
        return HostStationResponse.from(station);
    }

    @Transactional
    public HostStationResponse createCurrentProviderStationResponse(HostStationRequest request) {
        User provider = currentUserService.currentProvider();
        Station station = buildStation(publicIdGenerator.nextId("pvd"), provider, request);
        return HostStationResponse.from(stationRepository.saveAndFlush(station));
    }

    @Transactional
    public HostStationResponse updateCurrentProviderStationResponse(String stationId, HostStationRequest request) {
        Station station = stationRepository.findByIdAndProviderId(stationId, currentUserService.currentProviderId())
                .orElseThrow(() -> new NotFoundException("Station not found."));
        applyUpdate(station, request);
        return HostStationResponse.from(stationRepository.saveAndFlush(station));
    }

    @Transactional
    public void deleteCurrentProviderStation(String stationId) {
        Station station = stationRepository.findByIdAndProviderId(stationId, currentUserService.currentProviderId())
                .orElseThrow(() -> new NotFoundException("Station not found."));
        stationRepository.delete(station);
    }

    @Transactional
    public Station upsertCurrentProviderStation(HostStationRequest request) {
        User provider = currentUserService.currentProvider();
        Set<ConnectorType> connectorTypes = parseConnectorTypes(request.connectorTypes());
        Set<Amenity> amenities = parseAmenities(request.amenities());
        List<String> photoUrls = request.photoUrls() == null ? List.of() : request.photoUrls();
        boolean available = request.isAvailable() == null || request.isAvailable();

        Station station = stationRepository.findFirstByProviderIdOrderByIdAsc(provider.getId())
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

    private Station buildStation(String id, User provider, HostStationRequest request) {
        Set<ConnectorType> connectorTypes = parseConnectorTypes(request.connectorTypes());
        Set<Amenity> amenities = parseAmenities(request.amenities());
        List<String> photoUrls = request.photoUrls() == null ? List.of() : request.photoUrls();
        boolean available = request.isAvailable() == null || request.isAvailable();

        return new Station(
                id,
                provider,
                request.name(),
                request.address(),
                request.lat(),
                request.lng(),
                request.pricePerHour(),
                connectorTypes,
                amenities,
                photoUrls,
                available);
    }

    private void applyUpdate(Station station, HostStationRequest request) {
        Set<ConnectorType> connectorTypes = parseConnectorTypes(request.connectorTypes());
        Set<Amenity> amenities = parseAmenities(request.amenities());
        List<String> photoUrls = request.photoUrls() == null ? List.of() : request.photoUrls();
        boolean available = request.isAvailable() == null || request.isAvailable();

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
    }
}
