package com.app.venus.modules.vehicle.interfaces.dto.response;

import com.app.venus.modules.vehicle.domain.Vehicle;

public record VehicleResponse(
        String id,
        String brand,
        String model,
        int year,
        String connectorType,
        boolean isDefault) {

    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getConnectorType().getValue(),
                vehicle.isDefaultVehicle());
    }
}
