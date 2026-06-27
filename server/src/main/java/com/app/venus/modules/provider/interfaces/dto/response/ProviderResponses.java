package com.app.venus.modules.provider.interfaces.dto.response;

import com.app.venus.modules.provider.application.ProviderDiscoveryService.AvailabilityResult;
import com.app.venus.modules.provider.application.ProviderDiscoveryService.ProviderDetail;
import com.app.venus.modules.provider.application.ProviderDiscoveryService.ProviderSearchResult;

public final class ProviderResponses {
    private ProviderResponses() {
    }

    public record ProvidersResponse(int total, java.util.List<ProviderSummaryResponse> providers) {
        public static ProvidersResponse from(ProviderSearchResult result) {
            return new ProvidersResponse(
                    result.total(),
                    result.providers().stream().map(ProviderSummaryResponse::from).toList());
        }
    }

    public record ProviderSummaryResponse(
            String id,
            String name,
            String avatarUrl,
            String address,
            java.math.BigDecimal lat,
            java.math.BigDecimal lng,
            Double distanceKm,
            int pricePerHour,
            double rating,
            long reviewCount,
            java.util.List<String> connectorTypes,
            java.util.List<String> amenities,
            java.util.List<String> photoUrls,
            boolean isAvailable) {

        static ProviderSummaryResponse from(
                com.app.venus.modules.provider.application.ProviderDiscoveryService.ProviderSummary provider) {
            return new ProviderSummaryResponse(
                    provider.id(),
                    provider.name(),
                    provider.avatarUrl(),
                    provider.address(),
                    provider.lat(),
                    provider.lng(),
                    provider.distanceKm(),
                    provider.pricePerHour(),
                    provider.rating(),
                    provider.reviewCount(),
                    provider.connectorTypes(),
                    provider.amenities(),
                    provider.photoUrls(),
                    provider.isAvailable());
        }
    }

    public record ProviderDetailResponse(
            String id,
            String name,
            String avatarUrl,
            String address,
            java.math.BigDecimal lat,
            java.math.BigDecimal lng,
            Double distanceKm,
            int pricePerHour,
            double rating,
            long reviewCount,
            java.util.List<String> connectorTypes,
            java.util.List<String> amenities,
            java.util.List<String> photoUrls,
            boolean isAvailable,
            java.util.List<ReviewSummaryResponse> reviews) {

        public static ProviderDetailResponse from(ProviderDetail provider) {
            return new ProviderDetailResponse(
                    provider.id(),
                    provider.name(),
                    provider.avatarUrl(),
                    provider.address(),
                    provider.lat(),
                    provider.lng(),
                    provider.distanceKm(),
                    provider.pricePerHour(),
                    provider.rating(),
                    provider.reviewCount(),
                    provider.connectorTypes(),
                    provider.amenities(),
                    provider.photoUrls(),
                    provider.isAvailable(),
                    provider.reviews().stream().map(ReviewSummaryResponse::from).toList());
        }
    }

    public record ReviewSummaryResponse(
            String id,
            String authorName,
            String authorAvatarUrl,
            int rating,
            String comment,
            java.time.Instant createdAt) {

        static ReviewSummaryResponse from(
                com.app.venus.modules.provider.application.ProviderDiscoveryService.ReviewSummary review) {
            return new ReviewSummaryResponse(
                    review.id(),
                    review.authorName(),
                    review.authorAvatarUrl(),
                    review.rating(),
                    review.comment(),
                    review.createdAt());
        }
    }

    public record AvailabilityResponse(
            java.time.LocalDate date,
            java.util.List<BookedSlotResponse> bookedSlots) {

        public static AvailabilityResponse from(AvailabilityResult result) {
            return new AvailabilityResponse(
                    result.date(),
                    result.bookedSlots().stream().map(BookedSlotResponse::from).toList());
        }
    }

    public record BookedSlotResponse(java.time.OffsetDateTime startTime, java.time.OffsetDateTime endTime) {
        static BookedSlotResponse from(
                com.app.venus.modules.provider.application.ProviderDiscoveryService.BookedSlot slot) {
            return new BookedSlotResponse(slot.startTime(), slot.endTime());
        }
    }
}
