package com.app.venus.modules.order.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;

import com.app.venus.shared.exception.UnprocessableEntityException;

@Service
public class OrderPricingService {
    private static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.10");
    private static final BigDecimal MINUTES_PER_HOUR = new BigDecimal("60");

    public PriceBreakdown calculate(OffsetDateTime startTime, OffsetDateTime endTime, int pricePerHour) {
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new UnprocessableEntityException("End time must be after start time.");
        }

        long minutes = Duration.between(startTime, endTime).toMinutes();
        BigDecimal durationHours = new BigDecimal(minutes)
                .divide(MINUTES_PER_HOUR, 2, RoundingMode.HALF_UP);
        int subtotal = durationHours
                .multiply(new BigDecimal(pricePerHour))
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
        int serviceFee = new BigDecimal(subtotal)
                .multiply(SERVICE_FEE_RATE)
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();

        return new PriceBreakdown(durationHours, pricePerHour, subtotal, serviceFee, subtotal + serviceFee);
    }

    public record PriceBreakdown(
            BigDecimal durationHours,
            int pricePerHour,
            int subtotal,
            int serviceFee,
            int total) {
    }
}
