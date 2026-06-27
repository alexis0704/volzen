package com.app.venus.modules.vehicle.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.venus.modules.user.application.DemoCurrentUserService;
import com.app.venus.modules.vehicle.domain.Vehicle;
import com.app.venus.modules.vehicle.infrastructure.VehicleRepository;
import com.app.venus.shared.exception.ConflictException;

@SpringBootTest
@Transactional
class VehicleServiceTests {
    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private DemoCurrentUserService demoCurrentUserService;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();
        demoCurrentUserService.currentDriver();
    }

    @Test
    void firstVehicleIsDefault() {
        Vehicle vehicle = vehicleService.createCurrentDriverVehicle("VinFast", "VF8", 2024, "CCS");

        assertThat(vehicle.isDefaultVehicle()).isTrue();
    }

    @Test
    void settingVehicleDefaultUnsetsPreviousDefault() {
        Vehicle first = vehicleService.createCurrentDriverVehicle("VinFast", "VF8", 2024, "CCS");
        Vehicle second = vehicleService.createCurrentDriverVehicle("Toyota", "bZ4X", 2023, "Type 2");

        vehicleService.updateCurrentDriverVehicle(
                second.getId(),
                second.getBrand(),
                second.getModel(),
                second.getYear(),
                second.getConnectorType().getValue(),
                true);

        List<Vehicle> vehicles = vehicleRepository.findByDriverIdOrderByDefaultVehicleDescCreatedInstantAsc(
                demoCurrentUserService.currentDriverId());

        assertThat(vehicles).hasSize(2);
        assertThat(vehicleRepository.findById(first.getId()).orElseThrow().isDefaultVehicle()).isFalse();
        assertThat(vehicleRepository.findById(second.getId()).orElseThrow().isDefaultVehicle()).isTrue();
    }

    @Test
    void cannotDeleteOnlyVehicle() {
        Vehicle vehicle = vehicleService.createCurrentDriverVehicle("VinFast", "VF8", 2024, "CCS");

        assertThatThrownBy(() -> vehicleService.deleteCurrentDriverVehicle(vehicle.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Cannot delete the only vehicle on the account.");
    }
}
