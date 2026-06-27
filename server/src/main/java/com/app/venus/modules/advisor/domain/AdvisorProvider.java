package com.app.venus.modules.advisor.domain;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AdvisorProvider {
    OPENAI("openai"),
    OLLAMA("ollama"),
    MOCK("mock");

    private final String value;

    AdvisorProvider(String value) {
        this.value = value;
    }

    @JsonCreator
    public static AdvisorProvider fromValue(String value) {
        return Arrays.stream(values())
                .filter(provider -> provider.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported advisor provider: " + value));
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
