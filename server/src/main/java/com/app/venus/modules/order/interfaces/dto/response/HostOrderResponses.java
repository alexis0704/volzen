package com.app.venus.modules.order.interfaces.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import com.app.venus.modules.order.application.HostOrderService.HostOrderListResult;
import com.app.venus.modules.order.domain.Order;
import com.app.venus.modules.user.domain.User;
import com.app.venus.modules.vehicle.domain.Vehicle;

public final class HostOrderResponses {
    private HostOrderResponses() {
    }

    public record HostOrdersResponse(long total, List<HostOrderResponse> orders) {
        public static HostOrdersResponse from(HostOrderListResult result) {
            return new HostOrdersResponse(
                    result.total(),
                    result.orders().stream().map(HostOrderResponse::from).toList());
        }
    }

    public record HostOrderResponse(
            String id,
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            BigDecimal durationHours,
            int total,
            String status,
            DriverCompactResponse driver,
            VehicleCompactResponse vehicle) {

        static HostOrderResponse from(Order order) {
            return new HostOrderResponse(
                    order.getId(),
                    order.getStartTime(),
                    order.getEndTime(),
                    order.getDurationHours(),
                    order.getTotal(),
                    order.getStatus().getValue(),
                    DriverCompactResponse.from(order.getDriver()),
                    VehicleCompactResponse.from(order.getVehicle()));
        }
    }

    public record DriverCompactResponse(String id, String fullName, String avatarUrl) {
        static DriverCompactResponse from(User driver) {
            return new DriverCompactResponse(driver.getId(), driver.getFullName(), driver.getAvatarUrl());
        }
    }

    public record VehicleCompactResponse(
            String brand,
            String model,
            String connectorType,
            String plate,
            Integer batteryPercent) {

        static VehicleCompactResponse from(Vehicle vehicle) {
            return new VehicleCompactResponse(
                    vehicle.getBrand(),
                    vehicle.getModel(),
                    vehicle.getConnectorType().getValue(),
                    null,
                    null);
        }
    }

    public record UpdateHostOrderStatusResponse(String id, String status) {
        public static UpdateHostOrderStatusResponse from(Order order) {
            return new UpdateHostOrderStatusResponse(order.getId(), order.getStatus().getValue());
        }
    }
}
