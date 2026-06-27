package com.app.venus.shared.domain;

import java.util.Arrays;

public enum Amenity {
    COFFEE("Coffee"),
    WIFI("WiFi"),
    AIR_CONDITIONING("Air Conditioning"),
    RESTROOM("Restroom"),
    PARKING("Parking"),
    COVERED("Covered"),
    SECURITY("Security"),
    SNACKS("Snacks");

    private final String value;

    Amenity(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Amenity fromValue(String value) {
        return Arrays.stream(values())
                .filter(amenity -> amenity.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown amenity: " + value));
    }
}
