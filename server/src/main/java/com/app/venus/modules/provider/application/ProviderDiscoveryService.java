package com.app.venus.modules.provider.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.venus.modules.order.domain.Order;
import com.app.venus.modules.order.infrastructure.OrderRepository;
import com.app.venus.modules.provider.domain.Station;
import com.app.venus.modules.provider.infrastructure.StationRepository;
import com.app.venus.modules.review.domain.Review;
import com.app.venus.modules.review.infrastructure.ReviewRepository;
import com.app.venus.shared.domain.ConnectorType;
import com.app.venus.shared.domain.OrderStatus;
import com.app.venus.shared.exception.NotFoundException;
import com.app.venus.shared.exception.UnprocessableEntityException;

@Service
public class ProviderDiscoveryService {
    private static final double DEFAULT_RADIUS_KM = 5.0;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;
    private static final ZoneOffset BUSINESS_OFFSET = ZoneOffset.ofHours(7);

    private final StationRepository stationRepository;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final StationDistanceCalculator distanceCalculator;

    public ProviderDiscoveryService(
            StationRepository stationRepository,
            ReviewRepository reviewRepository,
            OrderRepository orderRepository,
            StationDistanceCalculator distanceCalculator) {
        this.stationRepository = stationRepository;
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.distanceCalculator = distanceCalculator;
    }

    @Transactional(readOnly = true)
    public ProviderSearchResult searchProviders(
            BigDecimal lat,
            BigDecimal lng,
            Double radiusKm,
            String connectorTypeValue,
            Integer maxPricePerHour,
            Integer limit,
            Integer offset) {
        double effectiveRadius = radiusKm == null ? DEFAULT_RADIUS_KM : radiusKm;
        int effectiveLimit = Math.min(limit == null ? DEFAULT_LIMIT : limit, MAX_LIMIT);
        int effectiveOffset = offset == null ? 0 : offset;
        ConnectorType connectorType = connectorTypeValue == null ? null : parseConnectorType(connectorTypeValue);

        List<Station> candidates = connectorType == null
                ? stationRepository.findSearchCandidates(maxPricePerHour)
                : stationRepository.findSearchCandidatesByConnectorType(connectorType, maxPricePerHour);

        List<ProviderSummary> matchingProviders = candidates
                .stream()
                .map(station -> toSummary(station, distanceKm(lat, lng, station)))
                .filter(provider -> provider.distanceKm() <= effectiveRadius)
                .sorted(Comparator.comparingDouble(ProviderSummary::distanceKm))
                .toList();

        List<ProviderSummary> page = matchingProviders.stream()
                .skip(Math.max(0, effectiveOffset))
                .limit(Math.max(0, effectiveLimit))
                .toList();

        return new ProviderSearchResult(matchingProviders.size(), page);
    }

    @Transactional(readOnly = true)
    public ProviderDetail getProviderDetail(String providerId) {
        Station station = stationRepository.findByIdAndAvailableTrue(providerId)
                .orElseThrow(() -> new NotFoundException("Provider not found."));
        ProviderSummary summary = toSummary(station, null);
        List<ReviewSummary> reviews = reviewRepository.findByProviderStationOrderByCreatedInstantDesc(station)
                .stream()
                .map(ReviewSummary::from)
                .toList();

        return ProviderDetail.from(summary, reviews);
    }

    @Transactional(readOnly = true)
    public AvailabilityResult getAvailability(String providerId, LocalDate date) {
        Station station = stationRepository.findByIdAndAvailableTrue(providerId)
                .orElseThrow(() -> new NotFoundException("Provider not found."));
        OffsetDateTime start = date.atStartOfDay().atOffset(BUSINESS_OFFSET);
        OffsetDateTime end = start.plusDays(1);
        List<BookedSlot> bookedSlots = orderRepository.findBookedSlotsForStationDate(
                station.getId(),
                start,
                end,
                OrderStatus.CANCELLED)
                .stream()
                .map(order -> new BookedSlot(order.getStartTime(), order.getEndTime()))
                .toList();

        return new AvailabilityResult(date, bookedSlots);
    }

    private ProviderSummary toSummary(Station station, Double distanceKm) {
        long reviewCount = reviewRepository.countByProviderStation(station);
        Double averageRating = reviewRepository.averageRatingByProviderStation(station);
        double rating = averageRating == null
                ? 0.0
                : BigDecimal.valueOf(averageRating).setScale(1, java.math.RoundingMode.HALF_UP).doubleValue();

        return new ProviderSummary(
                station.getId(),
                station.getName(),
                station.getProvider().getAvatarUrl(),
                station.getAddress(),
                station.getLat(),
                station.getLng(),
                distanceKm,
                station.getPricePerHour(),
                rating,
                reviewCount,
                station.getConnectorTypes().stream().map(ConnectorType::getValue).sorted().toList(),
                station.getAmenities().stream().map(com.app.venus.shared.domain.Amenity::getValue).sorted().toList(),
                station.getPhotoUrls(),
                station.isAvailable());
    }

    private double distanceKm(BigDecimal lat, BigDecimal lng, Station station) {
        return BigDecimal.valueOf(distanceCalculator.distanceKm(lat, lng, station.getLat(), station.getLng()))
                .setScale(1, java.math.RoundingMode.HALF_UP)
                .doubleValue();
    }

    private ConnectorType parseConnectorType(String value) {
        try {
            return ConnectorType.fromValue(value);
        } catch (IllegalArgumentException exception) {
            throw new UnprocessableEntityException("Connector type must be one of: Type 1, Type 2, CCS, CHAdeMO.");
        }
    }

    public record ProviderSearchResult(int total, List<ProviderSummary> providers) {
    }

    public record ProviderSummary(
            String id,
            String name,
            String avatarUrl,
            String address,
            BigDecimal lat,
            BigDecimal lng,
            Double distanceKm,
            int pricePerHour,
            double rating,
            long reviewCount,
            List<String> connectorTypes,
            List<String> amenities,
            List<String> photoUrls,
            boolean isAvailable) {
    }

    public record ProviderDetail(
            String id,
            String name,
            String avatarUrl,
            String address,
            BigDecimal lat,
            BigDecimal lng,
            Double distanceKm,
            int pricePerHour,
            double rating,
            long reviewCount,
            List<String> connectorTypes,
            List<String> amenities,
            List<String> photoUrls,
            boolean isAvailable,
            List<ReviewSummary> reviews) {

        static ProviderDetail from(ProviderSummary summary, List<ReviewSummary> reviews) {
            return new ProviderDetail(
                    summary.id(),
                    summary.name(),
                    summary.avatarUrl(),
                    summary.address(),
                    summary.lat(),
                    summary.lng(),
                    summary.distanceKm(),
                    summary.pricePerHour(),
                    summary.rating(),
                    summary.reviewCount(),
                    summary.connectorTypes(),
                    summary.amenities(),
                    summary.photoUrls(),
                    summary.isAvailable(),
                    reviews);
        }
    }

    public record ReviewSummary(
            String id,
            String authorName,
            String authorAvatarUrl,
            int rating,
            String comment,
            java.time.Instant createdAt) {

        static ReviewSummary from(Review review) {
            return new ReviewSummary(
                    review.getId(),
                    review.getAuthor().getFullName(),
                    review.getAuthor().getAvatarUrl(),
                    review.getRating(),
                    review.getComment(),
                    review.getCreatedInstant());
        }
    }

    public record AvailabilityResult(LocalDate date, List<BookedSlot> bookedSlots) {
    }

    public record BookedSlot(OffsetDateTime startTime, OffsetDateTime endTime) {
    }
}
