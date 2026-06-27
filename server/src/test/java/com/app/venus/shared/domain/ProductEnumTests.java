package com.app.venus.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProductEnumTests {
    @Test
    void roleUsesApiValues() {
        assertThat(Role.DRIVER.getValue()).isEqualTo("driver");
        assertThat(Role.PROVIDER.getValue()).isEqualTo("provider");
        assertThat(Role.ADMIN.getValue()).isEqualTo("admin");
        assertThat(Role.TECHNICIAN.getValue()).isEqualTo("technician");
        assertThat(Role.fromValue("driver")).isEqualTo(Role.DRIVER);
    }

    @Test
    void connectorTypeUsesApiValues() {
        assertThat(ConnectorType.TYPE_1.getValue()).isEqualTo("Type 1");
        assertThat(ConnectorType.TYPE_2.getValue()).isEqualTo("Type 2");
        assertThat(ConnectorType.CCS.getValue()).isEqualTo("CCS");
        assertThat(ConnectorType.CHADEMO.getValue()).isEqualTo("CHAdeMO");
        assertThat(ConnectorType.fromValue("Type 2")).isEqualTo(ConnectorType.TYPE_2);
    }

    @Test
    void orderStatusUsesApiValues() {
        assertThat(OrderStatus.PENDING.getValue()).isEqualTo("pending");
        assertThat(OrderStatus.CONFIRMED.getValue()).isEqualTo("confirmed");
        assertThat(OrderStatus.ACTIVE.getValue()).isEqualTo("active");
        assertThat(OrderStatus.COMPLETED.getValue()).isEqualTo("completed");
        assertThat(OrderStatus.CANCELLED.getValue()).isEqualTo("cancelled");
        assertThat(OrderStatus.fromValue("completed")).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void amenityUsesApiValues() {
        assertThat(Amenity.COFFEE.getValue()).isEqualTo("Coffee");
        assertThat(Amenity.WIFI.getValue()).isEqualTo("WiFi");
        assertThat(Amenity.AIR_CONDITIONING.getValue()).isEqualTo("Air Conditioning");
        assertThat(Amenity.RESTROOM.getValue()).isEqualTo("Restroom");
        assertThat(Amenity.fromValue("WiFi")).isEqualTo(Amenity.WIFI);
    }
}
