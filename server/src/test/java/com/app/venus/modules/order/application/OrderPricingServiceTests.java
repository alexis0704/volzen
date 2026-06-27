package com.app.venus.modules.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import com.app.venus.shared.exception.UnprocessableEntityException;

class OrderPricingServiceTests {
    private final OrderPricingService pricingService = new OrderPricingService();

    @Test
    void calculatesDurationSubtotalServiceFeeAndTotal() {
        OffsetDateTime start = OffsetDateTime.parse("2026-06-28T09:00:00+07:00");
        OffsetDateTime end = OffsetDateTime.parse("2026-06-28T11:30:00+07:00");

        OrderPricingService.PriceBreakdown price = pricingService.calculate(start, end, 25000);

        assertThat(price.durationHours()).isEqualByComparingTo("2.50");
        assertThat(price.pricePerHour()).isEqualTo(25000);
        assertThat(price.subtotal()).isEqualTo(62500);
        assertThat(price.serviceFee()).isEqualTo(6250);
        assertThat(price.total()).isEqualTo(68750);
    }

    @Test
    void rejectsInvalidTimeWindow() {
        OffsetDateTime start = OffsetDateTime.parse("2026-06-28T11:00:00+07:00");
        OffsetDateTime end = OffsetDateTime.parse("2026-06-28T09:00:00+07:00");

        assertThatThrownBy(() -> pricingService.calculate(start, end, 25000))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessage("End time must be after start time.");
    }
}
