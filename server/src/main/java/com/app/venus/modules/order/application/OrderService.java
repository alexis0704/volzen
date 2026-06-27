package com.app.venus.modules.order.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.venus.modules.order.domain.Order;
import com.app.venus.modules.order.infrastructure.OrderRepository;
import com.app.venus.modules.provider.domain.Station;
import com.app.venus.modules.provider.infrastructure.StationRepository;
import com.app.venus.modules.review.domain.Review;
import com.app.venus.modules.review.infrastructure.ReviewRepository;
import com.app.venus.modules.user.application.DemoCurrentUserService;
import com.app.venus.modules.user.domain.User;
import com.app.venus.modules.vehicle.domain.Vehicle;
import com.app.venus.modules.vehicle.infrastructure.VehicleRepository;
import com.app.venus.shared.domain.OrderStatus;
import com.app.venus.shared.domain.PublicIdGenerator;
import com.app.venus.shared.exception.ConflictException;
import com.app.venus.shared.exception.NotFoundException;
import com.app.venus.shared.exception.UnprocessableEntityException;

@Service
public class OrderService {
    private static final int DEFAULT_LIMIT = 20;
    private static final Set<OrderStatus> BLOCKING_STATUSES = Set.of(
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.ACTIVE);

    private final OrderRepository orderRepository;
    private final StationRepository stationRepository;
    private final VehicleRepository vehicleRepository;
    private final ReviewRepository reviewRepository;
    private final DemoCurrentUserService demoCurrentUserService;
    private final PublicIdGenerator publicIdGenerator;
    private final OrderPricingService orderPricingService;

    public OrderService(
            OrderRepository orderRepository,
            StationRepository stationRepository,
            VehicleRepository vehicleRepository,
            ReviewRepository reviewRepository,
            DemoCurrentUserService demoCurrentUserService,
            PublicIdGenerator publicIdGenerator,
            OrderPricingService orderPricingService) {
        this.orderRepository = orderRepository;
        this.stationRepository = stationRepository;
        this.vehicleRepository = vehicleRepository;
        this.reviewRepository = reviewRepository;
        this.demoCurrentUserService = demoCurrentUserService;
        this.publicIdGenerator = publicIdGenerator;
        this.orderPricingService = orderPricingService;
    }

    @Transactional
    public Order createCurrentDriverOrder(
            String providerId,
            String vehicleId,
            OffsetDateTime startTime,
            OffsetDateTime endTime) {
        User driver = demoCurrentUserService.currentDriver();
        Station station = stationRepository.findById(providerId)
                .orElseThrow(() -> new NotFoundException("Provider not found."));
        if (!station.isAvailable()) {
            throw new UnprocessableEntityException("Provider station is not available.");
        }

        Vehicle vehicle = vehicleRepository.findByIdAndDriverId(vehicleId, driver.getId())
                .orElseThrow(() -> new NotFoundException("Vehicle not found."));
        OrderPricingService.PriceBreakdown price = orderPricingService.calculate(
                startTime,
                endTime,
                station.getPricePerHour());

        if (!station.getConnectorTypes().contains(vehicle.getConnectorType())) {
            throw new UnprocessableEntityException("Vehicle connector is not compatible with this provider.");
        }

        if (orderRepository.existsOverlappingStationOrder(
                station.getId(),
                startTime,
                endTime,
                BLOCKING_STATUSES)) {
            throw new ConflictException("SLOT_UNAVAILABLE", "The requested time slot is no longer available.");
        }

        Order order = new Order(
                publicIdGenerator.nextId("ord"),
                station,
                vehicle,
                driver,
                startTime,
                endTime,
                price.durationHours(),
                price.pricePerHour(),
                price.subtotal(),
                price.serviceFee(),
                price.total(),
                OrderStatus.CONFIRMED);

        return orderRepository.saveAndFlush(order);
    }

    @Transactional(readOnly = true)
    public Order getCurrentDriverOrder(String orderId) {
        return orderRepository.findByIdAndDriverId(orderId, demoCurrentUserService.currentDriverId())
                .orElseThrow(() -> new NotFoundException("Order not found."));
    }

    @Transactional(readOnly = true)
    public OrderListResult listCurrentDriverOrders(String statusValue, Integer limit, Integer offset) {
        OrderStatus status = statusValue == null ? null : parseStatus(statusValue);
        int effectiveLimit = limit == null ? DEFAULT_LIMIT : Math.max(0, limit);
        int effectiveOffset = offset == null ? 0 : Math.max(0, offset);
        PageRequest page = PageRequest.of(
                effectiveLimit == 0 ? 0 : effectiveOffset / effectiveLimit,
                effectiveLimit == 0 ? 1 : effectiveLimit);

        List<Order> pageRows = status == null
                ? orderRepository.findByDriverIdOrderByCreatedInstantDesc(demoCurrentUserService.currentDriverId(), page)
                : orderRepository.findByDriverIdAndStatusOrderByCreatedInstantDesc(
                        demoCurrentUserService.currentDriverId(),
                        status,
                        page);

        if (effectiveLimit > 0 && effectiveOffset % effectiveLimit != 0) {
            pageRows = pageRows.stream()
                    .skip(effectiveOffset % effectiveLimit)
                    .limit(effectiveLimit)
                    .toList();
        }

        long total = status == null
                ? orderRepository.countByDriverId(demoCurrentUserService.currentDriverId())
                : orderRepository.countByDriverIdAndStatus(demoCurrentUserService.currentDriverId(), status);

        return new OrderListResult(total, effectiveLimit == 0 ? List.of() : pageRows);
    }

    @Transactional
    public Order cancelCurrentDriverOrder(String orderId) {
        Order order = getCurrentDriverOrder(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return order;
        }
        if (order.getStatus() == OrderStatus.ACTIVE || order.getStatus() == OrderStatus.COMPLETED) {
            throw new UnprocessableEntityException("Active and completed orders cannot be cancelled.");
        }

        order.cancel();
        return orderRepository.saveAndFlush(order);
    }

    @Transactional
    public Review createCurrentDriverReview(String orderId, int rating, String comment) {
        Order order = getCurrentDriverOrder(orderId);
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new UnprocessableEntityException("Only completed orders can be reviewed.");
        }
        if (reviewRepository.existsByOrder(order)) {
            throw new ConflictException("DUPLICATE_REVIEW", "This order has already been reviewed.");
        }

        Review review = new Review(
                publicIdGenerator.nextId("rev"),
                order,
                order.getProviderStation(),
                order.getDriver(),
                rating,
                comment);

        return reviewRepository.saveAndFlush(review);
    }

    private OrderStatus parseStatus(String value) {
        try {
            return OrderStatus.fromValue(value);
        } catch (IllegalArgumentException exception) {
            throw new UnprocessableEntityException("Status must be one of: pending, confirmed, active, completed, cancelled.");
        }
    }

    public record OrderListResult(long total, List<Order> orders) {
    }
}
