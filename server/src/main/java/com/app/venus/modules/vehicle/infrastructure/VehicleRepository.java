package com.app.venus.modules.vehicle.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.venus.modules.vehicle.domain.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    List<Vehicle> findByDriverIdOrderByDefaultVehicleDescCreatedInstantAsc(String driverId);

    Optional<Vehicle> findByIdAndDriverId(String id, String driverId);

    long countByDriverId(String driverId);

    Optional<Vehicle> findByDriverIdAndDefaultVehicleTrue(String driverId);
}
