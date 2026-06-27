package com.app.venus.modules.provider.domain;

import java.util.Arrays;

public enum BlockReason {
    BUSY("Busy"),
    MAINTENANCE("Maintenance"),
    PERSONAL("Personal"),
    OTHER("Other");

    private final String value;

    BlockReason(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static BlockReason fromValue(String value) {
        return Arrays.stream(values())
                .filter(reason -> reason.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown block reason: " + value));
    }
}
