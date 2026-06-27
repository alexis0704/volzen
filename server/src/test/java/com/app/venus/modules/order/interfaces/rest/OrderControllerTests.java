package com.app.venus.modules.order.interfaces.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.app.venus.modules.order.domain.Order;
import com.app.venus.modules.order.infrastructure.OrderRepository;
import com.app.venus.modules.provider.domain.Station;
import com.app.venus.modules.provider.infrastructure.StationRepository;
import com.app.venus.modules.review.domain.Review;
import com.app.venus.modules.review.infrastructure.ReviewRepository;
import com.app.venus.modules.user.application.DemoCurrentUserService;
import com.app.venus.modules.user.domain.User;
import com.app.venus.modules.user.infrastructure.UserRepository;
import com.app.venus.modules.vehicle.domain.Vehicle;
import com.app.venus.modules.vehicle.infrastructure.VehicleRepository;
import com.app.venus.shared.domain.Amenity;
import com.app.venus.shared.domain.ConnectorType;
import com.app.venus.shared.domain.OrderStatus;
import com.app.venus.shared.domain.Role;

@SpringBootTest(properties = "app.seed.demo-data=false")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private User driver;
    private User otherDriver;
    private Station station;
    private Station unavailableStation;
    private Vehicle vehicle;
    private Vehicle incompatibleVehicle;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        orderRepository.deleteAll();
        vehicleRepository.deleteAll();
        stationRepository.deleteAll();

        driver = userRepository.findById(DemoCurrentUserService.DEMO_DRIVER_ID)
                .orElseGet(() -> userRepository.saveAndFlush(new User(
                        DemoCurrentUserService.DEMO_DRIVER_ID,
                        "Demo Driver",
                        "driver@volzen.test",
                        Role.DRIVER,
                        "https://cdn.volzen.vn/avatars/usr_demo_driver.jpg")));
        otherDriver = userRepository.findById("usr_order_other_driver")
                .orElseGet(() -> userRepository.saveAndFlush(new User(
                        "usr_order_other_driver",
                        "Other Driver",
                        "other-driver@volzen.test",
                        Role.DRIVER,
                        "https://cdn.volzen.vn/avatars/other.jpg")));
        User provider = userRepository.findById("usr_order_provider")
                .orElseGet(() -> userRepository.saveAndFlush(new User(
                        "usr_order_provider",
                        "Minh Tuan",
                        "order-provider@volzen.test",
                        Role.PROVIDER,
                        "https://cdn.volzen.vn/avatars/pvd_p1.jpg")));

        station = stationRepository.saveAndFlush(new Station(
                "pvd_order",
                provider,
                "12 Nguyen Hue, District 1, Ho Chi Minh City",
                new BigDecimal("10.7769000"),
                new BigDecimal("106.7009000"),
                25000,
                Set.of(ConnectorType.CCS, ConnectorType.TYPE_2),
                Set.of(Amenity.COFFEE, Amenity.WIFI),
                List.of("https://cdn.volzen.vn/stations/pvd_order/photo_1.jpg"),
                true));
        unavailableStation = stationRepository.saveAndFlush(new Station(
                "pvd_order_unavailable",
                provider,
                "Closed station",
                new BigDecimal("10.7700000"),
                new BigDecimal("106.7000000"),
                25000,
                Set.of(ConnectorType.CCS),
                Set.of(Amenity.PARKING),
                List.of("https://cdn.volzen.vn/stations/pvd_order_unavailable/photo_1.jpg"),
                false));
        vehicle = vehicleRepository.saveAndFlush(new Vehicle(
                "veh_order",
                driver,
                "VinFast",
                "VF8",
                2024,
                ConnectorType.CCS,
                true));
        incompatibleVehicle = vehicleRepository.saveAndFlush(new Vehicle(
                "veh_order_incompatible",
                driver,
                "Nissan",
                "Leaf",
                2021,
                ConnectorType.CHADEMO,
                false));
    }

    @Test
    void createsOrderForDemoDriver() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson(station.getId(), vehicle.getId(), "2026-06-28T09:00:00+07:00", "2026-06-28T11:00:00+07:00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.providerId").value(station.getId()))
                .andExpect(jsonPath("$.vehicleId").value(vehicle.getId()))
                .andExpect(jsonPath("$.driverId").value(DemoCurrentUserService.DEMO_DRIVER_ID))
                .andExpect(jsonPath("$.durationHours").value(2.0))
                .andExpect(jsonPath("$.subtotal").value(50000))
                .andExpect(jsonPath("$.serviceFee").value(5000))
                .andExpect(jsonPath("$.total").value(55000))
                .andExpect(jsonPath("$.status").value("pending"))
                .andExpect(jsonPath("$.provider.name").value("Minh Tuan"));
    }

    @Test
    void rejectsBadProviderBadVehicleUnavailableProviderAndBadTime() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson("pvd_missing", vehicle.getId(), "2026-06-28T09:00:00+07:00", "2026-06-28T11:00:00+07:00")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson(station.getId(), "veh_missing", "2026-06-28T09:00:00+07:00", "2026-06-28T11:00:00+07:00")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson(unavailableStation.getId(), vehicle.getId(), "2026-06-28T09:00:00+07:00", "2026-06-28T11:00:00+07:00")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson(station.getId(), vehicle.getId(), "2026-06-28T11:00:00+07:00", "2026-06-28T09:00:00+07:00")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void rejectsIncompatibleConnectorAndSlotConflict() throws Exception {
        orderRepository.saveAndFlush(order(
                "ord_existing_conflict",
                driver,
                vehicle,
                station,
                "2026-06-28T09:00:00+07:00",
                "2026-06-28T11:00:00+07:00",
                OrderStatus.CONFIRMED));

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson(station.getId(), incompatibleVehicle.getId(), "2026-06-28T12:00:00+07:00", "2026-06-28T13:00:00+07:00")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson(station.getId(), vehicle.getId(), "2026-06-28T10:00:00+07:00", "2026-06-28T12:00:00+07:00")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("SLOT_UNAVAILABLE"));
    }

    @Test
    void getsOrderListsOrdersAndIsolatesOwnership() throws Exception {
        orderRepository.saveAndFlush(order(
                "ord_driver_confirmed",
                driver,
                vehicle,
                station,
                "2026-06-28T09:00:00+07:00",
                "2026-06-28T10:00:00+07:00",
                OrderStatus.CONFIRMED));
        orderRepository.saveAndFlush(order(
                "ord_driver_completed",
                driver,
                vehicle,
                station,
                "2026-06-20T09:00:00+07:00",
                "2026-06-20T10:00:00+07:00",
                OrderStatus.COMPLETED));
        Vehicle otherVehicle = vehicleRepository.saveAndFlush(new Vehicle(
                "veh_order_other",
                otherDriver,
                "VinFast",
                "VF9",
                2024,
                ConnectorType.CCS,
                true));
        orderRepository.saveAndFlush(order(
                "ord_other_driver",
                otherDriver,
                otherVehicle,
                station,
                "2026-06-30T09:00:00+07:00",
                "2026-06-30T10:00:00+07:00",
                OrderStatus.CONFIRMED));

        mockMvc.perform(get("/api/v1/orders/{orderId}", "ord_driver_confirmed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ord_driver_confirmed"))
                .andExpect(jsonPath("$.provider.id").value(station.getId()));

        mockMvc.perform(get("/api/v1/orders/{orderId}", "ord_other_driver"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        mockMvc.perform(get("/api/v1/me/orders").param("status", "confirmed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.orders", hasSize(1)))
                .andExpect(jsonPath("$.orders[0].id").value("ord_driver_confirmed"));

        mockMvc.perform(get("/api/v1/me/orders").param("limit", "1").param("offset", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.orders", hasSize(1)));
    }

    @Test
    void cancelsPendingAndConfirmedOrdersIdempotentlyAndRejectsTerminalStates() throws Exception {
        orderRepository.saveAndFlush(order(
                "ord_cancel_confirmed",
                driver,
                vehicle,
                station,
                "2026-06-28T09:00:00+07:00",
                "2026-06-28T10:00:00+07:00",
                OrderStatus.CONFIRMED));
        orderRepository.saveAndFlush(order(
                "ord_cancel_active",
                driver,
                vehicle,
                station,
                "2026-06-28T11:00:00+07:00",
                "2026-06-28T12:00:00+07:00",
                OrderStatus.ACTIVE));
        orderRepository.saveAndFlush(order(
                "ord_cancel_completed",
                driver,
                vehicle,
                station,
                "2026-06-20T11:00:00+07:00",
                "2026-06-20T12:00:00+07:00",
                OrderStatus.COMPLETED));
        Vehicle otherVehicle = vehicleRepository.saveAndFlush(new Vehicle(
                "veh_cancel_other",
                otherDriver,
                "VinFast",
                "VF9",
                2024,
                ConnectorType.CCS,
                true));
        orderRepository.saveAndFlush(order(
                "ord_cancel_other",
                otherDriver,
                otherVehicle,
                station,
                "2026-06-30T09:00:00+07:00",
                "2026-06-30T10:00:00+07:00",
                OrderStatus.CONFIRMED));

        mockMvc.perform(patch("/api/v1/orders/{orderId}/cancel", "ord_cancel_confirmed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ord_cancel_confirmed"))
                .andExpect(jsonPath("$.status").value("cancelled"));

        mockMvc.perform(patch("/api/v1/orders/{orderId}/cancel", "ord_cancel_confirmed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("cancelled"));

        mockMvc.perform(patch("/api/v1/orders/{orderId}/cancel", "ord_cancel_other"))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/v1/orders/{orderId}/cancel", "ord_cancel_active"))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(patch("/api/v1/orders/{orderId}/cancel", "ord_cancel_completed"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void createsReviewForCompletedOrderAndRejectsInvalidReviewCases() throws Exception {
        orderRepository.saveAndFlush(order(
                "ord_review_completed",
                driver,
                vehicle,
                station,
                "2026-06-20T09:00:00+07:00",
                "2026-06-20T10:00:00+07:00",
                OrderStatus.COMPLETED));
        orderRepository.saveAndFlush(order(
                "ord_review_confirmed",
                driver,
                vehicle,
                station,
                "2026-06-28T09:00:00+07:00",
                "2026-06-28T10:00:00+07:00",
                OrderStatus.CONFIRMED));
        Vehicle otherVehicle = vehicleRepository.saveAndFlush(new Vehicle(
                "veh_review_other",
                otherDriver,
                "VinFast",
                "VF9",
                2024,
                ConnectorType.CCS,
                true));
        orderRepository.saveAndFlush(order(
                "ord_review_other",
                otherDriver,
                otherVehicle,
                station,
                "2026-06-20T11:00:00+07:00",
                "2026-06-20T12:00:00+07:00",
                OrderStatus.COMPLETED));

        mockMvc.perform(post("/api/v1/orders/{orderId}/review", "ord_review_completed")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "rating": 5,
                          "comment": "Great host, fast charger, highly recommend."
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("ord_review_completed"))
                .andExpect(jsonPath("$.providerId").value(station.getId()))
                .andExpect(jsonPath("$.authorName").value("Demo Driver"))
                .andExpect(jsonPath("$.rating").value(5));

        mockMvc.perform(post("/api/v1/orders/{orderId}/review", "ord_review_completed")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "rating": 4,
                          "comment": "Still good."
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE_REVIEW"));

        mockMvc.perform(post("/api/v1/orders/{orderId}/review", "ord_review_confirmed")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "rating": 5,
                          "comment": "Not yet."
                        }
                        """))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(post("/api/v1/orders/{orderId}/review", "ord_review_other")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "rating": 5,
                          "comment": "Wrong driver."
                        }
                        """))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/orders/{orderId}/review", "ord_review_confirmed")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "rating": 6,
                          "comment": "Bad rating."
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    private String orderJson(String providerId, String vehicleId, String startTime, String endTime) {
        return """
                {
                  "providerId": "%s",
                  "vehicleId": "%s",
                  "startTime": "%s",
                  "endTime": "%s"
                }
                """.formatted(providerId, vehicleId, startTime, endTime);
    }

    private Order order(
            String id,
            User orderDriver,
            Vehicle orderVehicle,
            Station orderStation,
            String start,
            String end,
            OrderStatus status) {
        return new Order(
                id,
                orderStation,
                orderVehicle,
                orderDriver,
                OffsetDateTime.parse(start),
                OffsetDateTime.parse(end),
                new BigDecimal("1.00"),
                orderStation.getPricePerHour(),
                orderStation.getPricePerHour(),
                (int) Math.round(orderStation.getPricePerHour() * 0.10),
                (int) Math.round(orderStation.getPricePerHour() * 1.10),
                status);
    }
}
