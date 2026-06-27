package com.app.venus.modules.provider.interfaces.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.app.venus.modules.order.domain.Order;
import com.app.venus.modules.order.infrastructure.OrderRepository;
import com.app.venus.modules.provider.domain.Station;
import com.app.venus.modules.provider.infrastructure.StationRepository;
import com.app.venus.modules.review.domain.Review;
import com.app.venus.modules.review.infrastructure.ReviewRepository;
import com.app.venus.modules.user.domain.User;
import com.app.venus.modules.user.infrastructure.UserRepository;
import com.app.venus.modules.vehicle.domain.Vehicle;
import com.app.venus.modules.vehicle.infrastructure.VehicleRepository;
import com.app.venus.shared.domain.Amenity;
import com.app.venus.shared.domain.ConnectorType;
import com.app.venus.shared.domain.OrderStatus;
import com.app.venus.shared.domain.Role;

@SpringBootTest
@AutoConfigureMockMvc
class ProviderControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    private User driver;
    private Station nearStation;
    private Station farStation;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        orderRepository.deleteAll();
        vehicleRepository.deleteAll();
        stationRepository.deleteAll();

        driver = userRepository.findById("usr_provider_driver")
                .orElseGet(() -> userRepository.saveAndFlush(new User(
                        "usr_provider_driver",
                        "Provider Driver",
                        "provider-driver@volzen.test",
                        Role.DRIVER,
                        "https://cdn.volzen.vn/avatars/usr_provider_driver.jpg")));
        User nearProvider = userRepository.findById("usr_provider_near")
                .orElseGet(() -> userRepository.saveAndFlush(new User(
                        "usr_provider_near",
                        "Minh Tuan",
                        "near-provider@volzen.test",
                        Role.PROVIDER,
                        "https://cdn.volzen.vn/avatars/pvd_p1.jpg")));
        User farProvider = userRepository.findById("usr_provider_far")
                .orElseGet(() -> userRepository.saveAndFlush(new User(
                        "usr_provider_far",
                        "Far Provider",
                        "far-provider@volzen.test",
                        Role.PROVIDER,
                        "https://cdn.volzen.vn/avatars/pvd_far.jpg")));

        nearStation = stationRepository.saveAndFlush(new Station(
                "pvd_p1",
                nearProvider,
                "12 Nguyen Hue, District 1, Ho Chi Minh City",
                new BigDecimal("10.7769000"),
                new BigDecimal("106.7009000"),
                25000,
                Set.of(ConnectorType.CCS, ConnectorType.TYPE_2),
                Set.of(Amenity.COFFEE, Amenity.WIFI),
                List.of(
                        "https://cdn.volzen.vn/stations/pvd_p1/photo_1.jpg",
                        "https://cdn.volzen.vn/stations/pvd_p1/photo_2.jpg"),
                true));
        farStation = stationRepository.saveAndFlush(new Station(
                "pvd_far",
                farProvider,
                "Thu Duc City, Ho Chi Minh City",
                new BigDecimal("10.8500000"),
                new BigDecimal("106.8000000"),
                45000,
                Set.of(ConnectorType.CHADEMO),
                Set.of(Amenity.PARKING),
                List.of("https://cdn.volzen.vn/stations/pvd_far/photo_1.jpg"),
                true));
        vehicle = vehicleRepository.saveAndFlush(new Vehicle(
                "veh_provider_driver",
                driver,
                "VinFast",
                "VF8",
                2024,
                ConnectorType.CCS,
                true));

        Order completedOrder = orderRepository.saveAndFlush(order(
                "ord_provider_completed",
                nearStation,
                "2026-06-20T09:00:00+07:00",
                "2026-06-20T11:00:00+07:00",
                OrderStatus.COMPLETED));
        reviewRepository.saveAndFlush(new Review(
                "rev_provider_1",
                completedOrder,
                nearStation,
                driver,
                5,
                "Super convenient and fast charger."));
    }

    @Test
    void searchesProvidersWithDistanceFiltersAndRating() throws Exception {
        mockMvc.perform(get("/api/v1/providers")
                .param("lat", "10.7769")
                .param("lng", "106.7009")
                .param("radiusKm", "5")
                .param("connectorType", "CCS")
                .param("maxPricePerHour", "30000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.providers", hasSize(1)))
                .andExpect(jsonPath("$.providers[0].id").value("pvd_p1"))
                .andExpect(jsonPath("$.providers[0].name").value("Minh Tuan"))
                .andExpect(jsonPath("$.providers[0].distanceKm").value(0.0))
                .andExpect(jsonPath("$.providers[0].rating").value(5.0))
                .andExpect(jsonPath("$.providers[0].reviewCount").value(1))
                .andExpect(jsonPath("$.providers[0].connectorTypes", hasSize(2)));
    }

    @Test
    void capsLimitAtFiftyAndAppliesOffset() throws Exception {
        mockMvc.perform(get("/api/v1/providers")
                .param("lat", "10.7769")
                .param("lng", "106.7009")
                .param("radiusKm", "100")
                .param("limit", "100")
                .param("offset", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.providers", hasSize(1)))
                .andExpect(jsonPath("$.providers[0].id").value(farStation.getId()));
    }

    @Test
    void getsProviderDetailWithReviews() throws Exception {
        mockMvc.perform(get("/api/v1/providers/{providerId}", nearStation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("pvd_p1"))
                .andExpect(jsonPath("$.name").value("Minh Tuan"))
                .andExpect(jsonPath("$.reviews", hasSize(1)))
                .andExpect(jsonPath("$.reviews[0].id").value("rev_provider_1"))
                .andExpect(jsonPath("$.reviews[0].authorName").value("Provider Driver"));
    }

    @Test
    void unknownProviderReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/providers/{providerId}", "pvd_missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void getsProviderAvailabilityAndExcludesCancelledOrders() throws Exception {
        orderRepository.save(order(
                "ord_availability_confirmed",
                nearStation,
                "2026-06-28T09:00:00+07:00",
                "2026-06-28T11:00:00+07:00",
                OrderStatus.CONFIRMED));
        orderRepository.save(order(
                "ord_availability_cancelled",
                nearStation,
                "2026-06-28T14:00:00+07:00",
                "2026-06-28T15:00:00+07:00",
                OrderStatus.CANCELLED));
        orderRepository.flush();

        mockMvc.perform(get("/api/v1/providers/{providerId}/availability", nearStation.getId())
                .param("date", "2026-06-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-06-28"))
                .andExpect(jsonPath("$.bookedSlots", hasSize(1)))
                .andExpect(jsonPath("$.bookedSlots[0].startTime").value("2026-06-28T09:00:00+07:00"))
                .andExpect(jsonPath("$.bookedSlots[0].endTime").value("2026-06-28T11:00:00+07:00"));
    }

    @Test
    void invalidAvailabilityDateReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/providers/{providerId}/availability", nearStation.getId())
                .param("date", "not-a-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    private Order order(String id, Station station, String start, String end, OrderStatus status) {
        return new Order(
                id,
                station,
                vehicle,
                driver,
                OffsetDateTime.parse(start),
                OffsetDateTime.parse(end),
                new BigDecimal("2.00"),
                station.getPricePerHour(),
                station.getPricePerHour() * 2,
                (int) Math.round(station.getPricePerHour() * 2 * 0.10),
                (int) Math.round(station.getPricePerHour() * 2 * 1.10),
                status);
    }
}
