package com.app.venus.modules.order.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.venus.modules.order.application.OrderService;
import com.app.venus.modules.order.interfaces.dto.request.CreateOrderRequest;
import com.app.venus.modules.order.interfaces.dto.response.OrderResponses.CancelOrderResponse;
import com.app.venus.modules.order.interfaces.dto.response.OrderResponses.OrderDetailResponse;
import com.app.venus.modules.order.interfaces.dto.response.OrderResponses.OrdersResponse;
import com.app.venus.modules.review.interfaces.dto.request.CreateReviewRequest;
import com.app.venus.modules.review.interfaces.dto.response.ReviewResponse;
import com.app.venus.shared.web.ApiPaths;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.transaction.annotation.Transactional;

@Validated
@RestController
@Transactional
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(ApiPaths.API_V1 + "/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDetailResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return OrderDetailResponse.from(orderService.createCurrentDriverOrder(
                request.providerId(),
                request.vehicleId(),
                request.startTime(),
                request.endTime()));
    }

    @GetMapping(ApiPaths.API_V1 + "/orders/{orderId}")
    public OrderDetailResponse getOrder(@PathVariable String orderId) {
        return OrderDetailResponse.from(orderService.getCurrentDriverOrder(orderId));
    }

    @GetMapping(ApiPaths.API_V1 + "/me/orders")
    public OrdersResponse listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @Min(0) Integer limit,
            @RequestParam(required = false) @Min(0) Integer offset) {
        return OrdersResponse.from(orderService.listCurrentDriverOrders(status, limit, offset));
    }

    @PatchMapping(ApiPaths.API_V1 + "/orders/{orderId}/cancel")
    public CancelOrderResponse cancelOrder(@PathVariable String orderId) {
        return CancelOrderResponse.from(orderService.cancelCurrentDriverOrder(orderId));
    }

    @PostMapping(ApiPaths.API_V1 + "/orders/{orderId}/review")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(
            @PathVariable String orderId,
            @Valid @RequestBody CreateReviewRequest request) {
        return ReviewResponse.from(orderService.createCurrentDriverReview(
                orderId,
                request.rating(),
                request.comment()));
    }
}
