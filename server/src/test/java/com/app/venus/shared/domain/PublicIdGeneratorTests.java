package com.app.venus.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PublicIdGeneratorTests {
    @Test
    void generatesPrefixedIds() {
        PublicIdGenerator generator = new PublicIdGenerator();

        String id = generator.nextId("usr");

        assertThat(id).startsWith("usr_");
        assertThat(id).hasSize(14);
    }
}
