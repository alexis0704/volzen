package com.app.venus.modules.provider.interfaces.rest;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.app.venus.modules.provider.application.HostStationService;
import com.app.venus.modules.provider.interfaces.dto.request.HostStationRequest;
import com.app.venus.modules.provider.interfaces.dto.response.HostStationResponse;
import com.app.venus.shared.web.ApiPaths;

import jakarta.validation.Valid;

@RestController
public class HostStationController {
    private final HostStationService hostStationService;

    public HostStationController(HostStationService hostStationService) {
        this.hostStationService = hostStationService;
    }

    @GetMapping(ApiPaths.API_V1 + "/me/station")
    public HostStationResponse getCurrentProviderStation() {
        return hostStationService.getCurrentProviderStationResponse();
    }

    @PutMapping(ApiPaths.API_V1 + "/me/station")
    public HostStationResponse upsertCurrentProviderStation(@Valid @RequestBody HostStationRequest request) {
        return hostStationService.upsertCurrentProviderStationResponse(request);
    }

    @GetMapping(ApiPaths.API_V1 + "/me/stations")
    public List<HostStationResponse> listCurrentProviderStations() {
        return hostStationService.listCurrentProviderStationResponses();
    }

    @PostMapping(ApiPaths.API_V1 + "/me/stations")
    public HostStationResponse createCurrentProviderStation(@Valid @RequestBody HostStationRequest request) {
        return hostStationService.createCurrentProviderStationResponse(request);
    }

    @PutMapping(ApiPaths.API_V1 + "/me/stations/{stationId}")
    public HostStationResponse updateCurrentProviderStation(
            @PathVariable String stationId,
            @Valid @RequestBody HostStationRequest request) {
        return hostStationService.updateCurrentProviderStationResponse(stationId, request);
    }

    @DeleteMapping(ApiPaths.API_V1 + "/me/stations/{stationId}")
    public void deleteCurrentProviderStation(@PathVariable String stationId) {
        hostStationService.deleteCurrentProviderStation(stationId);
    }
}
