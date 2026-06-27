package com.app.venus.shared.domain;

import java.util.Arrays;

public enum ConnectorType {
    TYPE_1("Type 1"),
    TYPE_2("Type 2"),
    CCS("CCS"),
    CHADEMO("CHAdeMO");

    private final String value;

    ConnectorType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ConnectorType fromValue(String value) {
        return Arrays.stream(values())
                .filter(connectorType -> connectorType.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown connector type: " + value));
    }
}
