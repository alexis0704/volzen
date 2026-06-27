package com.app.venus.modules.order.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.app.venus.modules.provider.domain.Station;
import com.app.venus.modules.user.domain.User;
import com.app.venus.modules.vehicle.domain.Vehicle;
import com.app.venus.shared.auditing.Auditable;
import com.app.venus.shared.domain.OrderStatus;
import com.app.venus.shared.exception.InvalidStatusTransitionException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "ChargingOrder")
@Table(name = "charging_order")
public class Order extends Auditable {
    @Id
    @Column(length = 40)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private Station providerStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private User driver;

    private OffsetDateTime startTime;

    private OffsetDateTime endTime;

    @Column(precision = 10, scale = 2)
    private BigDecimal durationHours;

    private Integer pricePerHour;

    private Integer subtotal;

    private Integer serviceFee;

    private Integer total;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private OrderStatus status;

    protected Order() {
    }

    public Order(String id) {
        this.id = id;
    }

    public Order(
            String id,
            Station providerStation,
            Vehicle vehicle,
            User driver,
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            BigDecimal durationHours,
            int pricePerHour,
            int subtotal,
            int serviceFee,
            int total,
            OrderStatus status) {
        this.id = id;
        this.providerStation = providerStation;
        this.vehicle = vehicle;
        this.driver = driver;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationHours = durationHours;
        this.pricePerHour = pricePerHour;
        this.subtotal = subtotal;
        this.serviceFee = serviceFee;
        this.total = total;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public Station getProviderStation() {
        return providerStation;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public User getDriver() {
        return driver;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public BigDecimal getDurationHours() {
        return durationHours;
    }

    public Integer getPricePerHour() {
        return pricePerHour;
    }

    public Integer getSubtotal() {
        return subtotal;
    }

    public Integer getServiceFee() {
        return serviceFee;
    }

    public Integer getTotal() {
        return total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

    public void transitionByProvider(OrderStatus nextStatus) {
        if (status == OrderStatus.PENDING
                && (nextStatus == OrderStatus.CONFIRMED || nextStatus == OrderStatus.CANCELLED)) {
            this.status = nextStatus;
            return;
        }
        if (status == OrderStatus.CONFIRMED && nextStatus == OrderStatus.ACTIVE) {
            this.status = nextStatus;
            return;
        }
        if (status == OrderStatus.ACTIVE && nextStatus == OrderStatus.COMPLETED) {
            this.status = nextStatus;
            return;
        }
        throw new InvalidStatusTransitionException(
                "Cannot transition from '%s' to '%s'.".formatted(status.getValue(), nextStatus.getValue()));
    }
}
