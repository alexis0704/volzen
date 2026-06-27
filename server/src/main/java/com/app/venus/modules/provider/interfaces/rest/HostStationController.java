package com.app.venus.modules.provider.interfaces.rest;

import org.springframework.web.bind.annotation.GetMapping;
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
        return HostStationResponse.from(hostStationService.getCurrentProviderStation());
    }

    @PutMapping(ApiPaths.API_V1 + "/me/station")
    public HostStationResponse upsertCurrentProviderStation(@Valid @RequestBody HostStationRequest request) {
        return HostStationResponse.from(hostStationService.upsertCurrentProviderStation(request));
    }
}
