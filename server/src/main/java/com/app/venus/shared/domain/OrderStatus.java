package com.app.venus.shared.domain;

import java.util.Arrays;

public enum OrderStatus {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    ACTIVE("active"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static OrderStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(orderStatus -> orderStatus.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown order status: " + value));
    }
}
