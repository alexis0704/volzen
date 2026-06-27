package com.app.venus.modules.vehicle.domain;

import com.app.venus.modules.user.domain.User;
import com.app.venus.shared.auditing.Auditable;
import com.app.venus.shared.domain.ConnectorType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicle")
public class Vehicle extends Auditable {
    @Id
    @Column(length = 40)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(name = "vehicle_year", nullable = false)
    private int year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ConnectorType connectorType;

    @Column(nullable = false)
    private boolean defaultVehicle;

    protected Vehicle() {
    }

    public Vehicle(
            String id,
            User driver,
            String brand,
            String model,
            int year,
            ConnectorType connectorType,
            boolean defaultVehicle) {
        this.id = id;
        this.driver = driver;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.connectorType = connectorType;
        this.defaultVehicle = defaultVehicle;
    }

    public String getId() {
        return id;
    }

    public User getDriver() {
        return driver;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getYear() {
        return year;
    }

    public ConnectorType getConnectorType() {
        return connectorType;
    }

    public boolean isDefaultVehicle() {
        return defaultVehicle;
    }

    public void update(String brand, String model, Integer year, ConnectorType connectorType) {
        if (brand != null) {
            this.brand = brand;
        }
        if (model != null) {
            this.model = model;
        }
        if (year != null) {
            this.year = year;
        }
        if (connectorType != null) {
            this.connectorType = connectorType;
        }
    }

    public void setDefaultVehicle(boolean defaultVehicle) {
        this.defaultVehicle = defaultVehicle;
    }
}
