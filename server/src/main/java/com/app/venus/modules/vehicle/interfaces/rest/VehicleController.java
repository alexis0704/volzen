package com.app.venus.modules.vehicle.interfaces.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.venus.modules.vehicle.application.VehicleService;
import com.app.venus.modules.vehicle.interfaces.dto.request.VehicleRequest;
import com.app.venus.modules.vehicle.interfaces.dto.response.VehicleResponse;
import com.app.venus.modules.vehicle.interfaces.dto.response.VehiclesResponse;
import com.app.venus.shared.web.ApiPaths;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/me/vehicles")
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public VehiclesResponse listVehicles() {
        List<VehicleResponse> vehicles = vehicleService.listCurrentDriverVehicles()
                .stream()
                .map(VehicleResponse::from)
                .toList();
        return new VehiclesResponse(vehicles);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse createVehicle(@Valid @RequestBody VehicleRequest request) {
        return VehicleResponse.from(vehicleService.createCurrentDriverVehicle(
                request.brand(),
                request.model(),
                request.year(),
                request.connectorType()));
    }

    @PatchMapping("/{vehicleId}")
    public VehicleResponse updateVehicle(
            @PathVariable String vehicleId,
            @Valid @RequestBody VehicleRequest request) {
        return VehicleResponse.from(vehicleService.updateCurrentDriverVehicle(
                vehicleId,
                request.brand(),
                request.model(),
                request.year(),
                request.connectorType(),
                request.isDefault()));
    }

    @DeleteMapping("/{vehicleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVehicle(@PathVariable String vehicleId) {
        vehicleService.deleteCurrentDriverVehicle(vehicleId);
    }
}
