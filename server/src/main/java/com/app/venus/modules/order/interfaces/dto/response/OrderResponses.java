package com.app.venus.modules.order.interfaces.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import com.app.venus.modules.order.application.OrderService.OrderListResult;
import com.app.venus.modules.order.domain.Order;
import com.app.venus.modules.provider.domain.Station;

public final class OrderResponses {
    private OrderResponses() {
    }

    public record OrderDetailResponse(
            String id,
            String providerId,
            String vehicleId,
            String driverId,
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            BigDecimal durationHours,
            int pricePerHour,
            int subtotal,
            int serviceFee,
            int total,
            String status,
            Instant createdAt,
            ProviderSummaryResponse provider) {

        public static OrderDetailResponse from(Order order) {
            return new OrderDetailResponse(
                    order.getId(),
                    order.getProviderStation().getId(),
                    order.getVehicle().getId(),
                    order.getDriver().getId(),
                    order.getStartTime(),
                    order.getEndTime(),
                    order.getDurationHours(),
                    order.getPricePerHour(),
                    order.getSubtotal(),
                    order.getServiceFee(),
                    order.getTotal(),
                    order.getStatus().getValue(),
                    order.getCreatedInstant(),
                    ProviderSummaryResponse.from(order.getProviderStation()));
        }
    }

    public record OrdersResponse(long total, List<OrderSummaryResponse> orders) {
        public static OrdersResponse from(OrderListResult result) {
            return new OrdersResponse(
                    result.total(),
                    result.orders().stream().map(OrderSummaryResponse::from).toList());
        }
    }

    public record OrderSummaryResponse(
            String id,
            String providerId,
            String vehicleId,
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            int total,
            String status,
            ProviderCompactResponse provider) {

        static OrderSummaryResponse from(Order order) {
            return new OrderSummaryResponse(
                    order.getId(),
                    order.getProviderStation().getId(),
                    order.getVehicle().getId(),
                    order.getStartTime(),
                    order.getEndTime(),
                    order.getTotal(),
                    order.getStatus().getValue(),
                    ProviderCompactResponse.from(order.getProviderStation()));
        }
    }

    public record ProviderSummaryResponse(
            String id,
            String name,
            String address,
            BigDecimal lat,
            BigDecimal lng,
            String avatarUrl) {

        static ProviderSummaryResponse from(Station station) {
            return new ProviderSummaryResponse(
                    station.getId(),
                    station.getProvider().getFullName(),
                    station.getAddress(),
                    station.getLat(),
                    station.getLng(),
                    station.getProvider().getAvatarUrl());
        }
    }

    public record ProviderCompactResponse(
            String id,
            String name,
            String address,
            String avatarUrl) {

        static ProviderCompactResponse from(Station station) {
            return new ProviderCompactResponse(
                    station.getId(),
                    station.getProvider().getFullName(),
                    station.getAddress(),
                    station.getProvider().getAvatarUrl());
        }
    }

    public record CancelOrderResponse(String id, String status) {
        public static CancelOrderResponse from(Order order) {
            return new CancelOrderResponse(order.getId(), order.getStatus().getValue());
        }
    }
}
