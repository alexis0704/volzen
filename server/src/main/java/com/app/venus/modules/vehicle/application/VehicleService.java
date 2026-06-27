package com.app.venus.modules.vehicle.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.venus.modules.user.application.DemoCurrentUserService;
import com.app.venus.modules.user.domain.User;
import com.app.venus.modules.vehicle.domain.Vehicle;
import com.app.venus.modules.vehicle.infrastructure.VehicleRepository;
import com.app.venus.shared.domain.ConnectorType;
import com.app.venus.shared.domain.PublicIdGenerator;
import com.app.venus.shared.exception.ConflictException;
import com.app.venus.shared.exception.NotFoundException;
import com.app.venus.shared.exception.UnprocessableEntityException;

@Service
public class VehicleService {
    private final DemoCurrentUserService demoCurrentUserService;
    private final VehicleRepository vehicleRepository;
    private final PublicIdGenerator idGenerator;

    public VehicleService(
            DemoCurrentUserService demoCurrentUserService,
            VehicleRepository vehicleRepository,
            PublicIdGenerator idGenerator) {
        this.demoCurrentUserService = demoCurrentUserService;
        this.vehicleRepository = vehicleRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional(readOnly = true)
    public List<Vehicle> listCurrentDriverVehicles() {
        return vehicleRepository.findByDriverIdOrderByDefaultVehicleDescCreatedInstantAsc(
                demoCurrentUserService.currentDriverId());
    }

    @Transactional
    public Vehicle createCurrentDriverVehicle(
            String brand,
            String model,
            int year,
            String connectorTypeValue) {
        User driver = demoCurrentUserService.currentDriver();
        boolean firstVehicle = vehicleRepository.countByDriverId(driver.getId()) == 0;
        Vehicle vehicle = new Vehicle(
                idGenerator.nextId("veh"),
                driver,
                brand,
                model,
                year,
                parseConnectorType(connectorTypeValue),
                firstVehicle);

        return vehicleRepository.saveAndFlush(vehicle);
    }

    @Transactional
    public Vehicle updateCurrentDriverVehicle(
            String vehicleId,
            String brand,
            String model,
            Integer year,
            String connectorTypeValue,
            Boolean defaultVehicle) {
        String driverId = demoCurrentUserService.currentDriverId();
        Vehicle vehicle = vehicleRepository.findByIdAndDriverId(vehicleId, driverId)
                .orElseThrow(() -> new NotFoundException("Vehicle not found."));

        vehicle.update(
                brand,
                model,
                year,
                connectorTypeValue == null ? null : parseConnectorType(connectorTypeValue));

        if (Boolean.TRUE.equals(defaultVehicle)) {
            makeDefault(driverId, vehicle);
        } else if (Boolean.FALSE.equals(defaultVehicle) && vehicle.isDefaultVehicle()) {
            throw new UnprocessableEntityException("A default vehicle cannot be unset directly.");
        }

        return vehicle;
    }

    @Transactional
    public void deleteCurrentDriverVehicle(String vehicleId) {
        String driverId = demoCurrentUserService.currentDriverId();
        Vehicle vehicle = vehicleRepository.findByIdAndDriverId(vehicleId, driverId)
                .orElseThrow(() -> new NotFoundException("Vehicle not found."));

        if (vehicleRepository.countByDriverId(driverId) <= 1) {
            throw new ConflictException("Cannot delete the only vehicle on the account.");
        }

        boolean wasDefault = vehicle.isDefaultVehicle();
        vehicleRepository.delete(vehicle);
        vehicleRepository.flush();

        if (wasDefault) {
            vehicleRepository.findByDriverIdOrderByDefaultVehicleDescCreatedInstantAsc(driverId)
                    .stream()
                    .findFirst()
                    .ifPresent(nextDefault -> nextDefault.setDefaultVehicle(true));
        }
    }

    private void makeDefault(String driverId, Vehicle vehicle) {
        vehicleRepository.findByDriverIdAndDefaultVehicleTrue(driverId)
                .filter(currentDefault -> !currentDefault.getId().equals(vehicle.getId()))
                .ifPresent(currentDefault -> currentDefault.setDefaultVehicle(false));
        vehicle.setDefaultVehicle(true);
    }

    private ConnectorType parseConnectorType(String value) {
        try {
            return ConnectorType.fromValue(value);
        } catch (IllegalArgumentException exception) {
            throw new UnprocessableEntityException("Connector type must be one of: Type 1, Type 2, CCS, CHAdeMO.");
        }
    }
}
