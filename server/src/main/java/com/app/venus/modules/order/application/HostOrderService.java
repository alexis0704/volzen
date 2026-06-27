package com.app.venus.modules.order.application;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.venus.modules.order.domain.Order;
import com.app.venus.modules.order.infrastructure.OrderRepository;
import com.app.venus.modules.user.application.DemoCurrentUserService;
import com.app.venus.shared.domain.OrderStatus;
import com.app.venus.shared.exception.NotFoundException;
import com.app.venus.shared.exception.UnprocessableEntityException;

@Service
public class HostOrderService {
    private static final int DEFAULT_LIMIT = 20;

    private final OrderRepository orderRepository;
    private final DemoCurrentUserService demoCurrentUserService;

    public HostOrderService(OrderRepository orderRepository, DemoCurrentUserService demoCurrentUserService) {
        this.orderRepository = orderRepository;
        this.demoCurrentUserService = demoCurrentUserService;
    }

    @Transactional(readOnly = true)
    public HostOrderListResult listCurrentProviderOrders(String statusValue, Integer limit, Integer offset) {
        OrderStatus status = statusValue == null ? null : parseStatus(statusValue);
        int effectiveLimit = limit == null ? DEFAULT_LIMIT : Math.max(0, limit);
        int effectiveOffset = offset == null ? 0 : Math.max(0, offset);
        PageRequest page = PageRequest.of(
                effectiveLimit == 0 ? 0 : effectiveOffset / effectiveLimit,
                effectiveLimit == 0 ? 1 : effectiveLimit);

        String providerId = demoCurrentUserService.currentProviderId();
        List<Order> pageRows = status == null
                ? orderRepository.findByProviderStationProviderIdOrderByCreatedInstantDesc(providerId, page)
                : orderRepository.findByProviderStationProviderIdAndStatusOrderByCreatedInstantDesc(providerId, status, page);

        if (effectiveLimit > 0 && effectiveOffset % effectiveLimit != 0) {
            pageRows = pageRows.stream()
                    .skip(effectiveOffset % effectiveLimit)
                    .limit(effectiveLimit)
                    .toList();
        }

        long total = status == null
                ? orderRepository.countByProviderStationProviderId(providerId)
                : orderRepository.countByProviderStationProviderIdAndStatus(providerId, status);
        return new HostOrderListResult(total, effectiveLimit == 0 ? List.of() : pageRows);
    }

    @Transactional
    public Order updateCurrentProviderOrderStatus(String orderId, String statusValue) {
        OrderStatus nextStatus = parseStatus(statusValue);
        Order order = orderRepository.findByIdAndProviderStationProviderId(
                orderId,
                demoCurrentUserService.currentProviderId())
                .orElseThrow(() -> new NotFoundException("Order not found."));
        order.transitionByProvider(nextStatus);
        return orderRepository.saveAndFlush(order);
    }

    private OrderStatus parseStatus(String value) {
        try {
            return OrderStatus.fromValue(value);
        } catch (IllegalArgumentException exception) {
            throw new UnprocessableEntityException("Status must be one of: pending, confirmed, active, completed, cancelled.");
        }
    }

    public record HostOrderListResult(long total, List<Order> orders) {
    }
}
