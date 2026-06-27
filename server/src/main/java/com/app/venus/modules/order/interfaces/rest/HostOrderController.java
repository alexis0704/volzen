package com.app.venus.modules.order.interfaces.rest;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.venus.modules.order.application.HostOrderService;
import com.app.venus.modules.order.interfaces.dto.request.UpdateHostOrderStatusRequest;
import com.app.venus.modules.order.interfaces.dto.response.HostOrderResponses.HostOrdersResponse;
import com.app.venus.modules.order.interfaces.dto.response.HostOrderResponses.UpdateHostOrderStatusResponse;
import com.app.venus.shared.web.ApiPaths;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@Validated
@RestController
public class HostOrderController {
    private final HostOrderService hostOrderService;

    public HostOrderController(HostOrderService hostOrderService) {
        this.hostOrderService = hostOrderService;
    }

    @GetMapping(ApiPaths.API_V1 + "/me/station/orders")
    public HostOrdersResponse listCurrentProviderOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @Min(0) Integer limit,
            @RequestParam(required = false) @Min(0) Integer offset) {
        return HostOrdersResponse.from(hostOrderService.listCurrentProviderOrders(status, limit, offset));
    }

    @PatchMapping(ApiPaths.API_V1 + "/me/station/orders/{orderId}/status")
    public UpdateHostOrderStatusResponse updateCurrentProviderOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateHostOrderStatusRequest request) {
        return UpdateHostOrderStatusResponse.from(hostOrderService.updateCurrentProviderOrderStatus(
                orderId,
                request.status()));
    }
}
