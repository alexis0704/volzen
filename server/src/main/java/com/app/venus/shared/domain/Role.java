package com.app.venus.shared.domain;

import java.util.Arrays;

public enum Role {
    DRIVER("driver"),
    PROVIDER("provider"),
    ADMIN("admin"),
    TECHNICIAN("technician");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Role fromValue(String value) {
        return Arrays.stream(values())
                .filter(role -> role.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + value));
    }
}
