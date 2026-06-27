package com.app.venus.modules.order.interfaces.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.app.venus.modules.order.domain.Order;
import com.app.venus.modules.order.infrastructure.OrderRepository;
import com.app.venus.modules.provider.domain.Station;
import com.app.venus.modules.provider.infrastructure.StationRepository;
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

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class HostOrderControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private Station station;
    private Station otherStation;
    private Vehicle vehicle;
    private User driver;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        orderRepository.deleteAll();
        vehicleRepository.deleteAll();
        stationRepository.deleteAll();

        User provider = userRepository.findById(DemoCurrentUserService.DEMO_PROVIDER_ID)
                .orElseGet(() -> userRepository.saveAndFlush(new User(
                        DemoCurrentUserService.DEMO_PROVIDER_ID,
                        "Minh Tuan",
                        "p1@volzen.test",
                        Role.PROVIDER,
                        "https://cdn.volzen.vn/avatars/pvd_p1.jpg")));
        User otherProvider = userRepository.findById("usr_host_controller_other_provider")
                .orElseGet(() -> userRepository.saveAndFlush(new User(
                        "usr_host_controller_other_provider",
                        "Other Provider",
                        "other-provider@volzen.test",
                        Role.PROVIDER,
                        null)));
        driver = userRepository.findById("usr_host_controller_driver")
                .orElseGet(() -> userRepository.saveAndFlush(new User(
                        "usr_host_controller_driver",
                        "Lan Anh",
                        "lan-anh@volzen.test",
                        Role.DRIVER,
                        "https://cdn.volzen.vn/avatars/lan.jpg")));
        station = stationRepository.saveAndFlush(station("pvd_host_controller", provider));
        otherStation = stationRepository.saveAndFlush(station("pvd_host_controller_other", otherProvider));
        vehicle = vehicleRepository.saveAndFlush(new Vehicle(
                "veh_host_controller",
                driver,
                "VinFast",
                "VF8",
                2024,
                ConnectorType.CCS,
                true));
    }

    @Test
    void listsFiltersAndPaginatesHostOrders() throws Exception {
        orderRepository.save(order("ord_host_controller_pending", station, OrderStatus.PENDING));
        orderRepository.save(order("ord_host_controller_completed", station, OrderStatus.COMPLETED));
        orderRepository.save(order("ord_host_controller_other", otherStation, OrderStatus.PENDING));
        orderRepository.flush();

        mockMvc.perform(get("/api/v1/me/station/orders").param("status", "pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.orders", hasSize(1)))
                .andExpect(jsonPath("$.orders[0].id").value("ord_host_controller_pending"))
                .andExpect(jsonPath("$.orders[0].driver.fullName").value("Lan Anh"))
                .andExpect(jsonPath("$.orders[0].vehicle.connectorType").value("CCS"))
                .andExpect(jsonPath("$.orders[0].vehicle.plate").doesNotExist());
    }

    @Test
    void updatesHostOrderStatus() throws Exception {
        orderRepository.saveAndFlush(order("ord_host_accept", station, OrderStatus.PENDING));

        mockMvc.perform(patch("/api/v1/me/station/orders/{orderId}/status", "ord_host_accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "status": "confirmed" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ord_host_accept"))
                .andExpect(jsonPath("$.status").value("confirmed"));
    }

    @Test
    void invalidTransitionAndUnknownOrderUseProductErrors() throws Exception {
        orderRepository.saveAndFlush(order("ord_host_completed", station, OrderStatus.COMPLETED));

        mockMvc.perform(patch("/api/v1/me/station/orders/{orderId}/status", "ord_host_completed")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "status": "confirmed" }
                        """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("INVALID_STATUS_TRANSITION"));

        mockMvc.perform(patch("/api/v1/me/station/orders/{orderId}/status", "ord_missing")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "status": "confirmed" }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    private Station station(String id, User provider) {
        return new Station(
                id,
                provider,
                "Host Controller Station",
                "12 Nguyen Hue",
                new BigDecimal("10.7769000"),
                new BigDecimal("106.7009000"),
                25000,
                Set.of(ConnectorType.CCS),
                Set.of(Amenity.WIFI),
                List.of(),
                true);
    }

    private Order order(String id, Station station, OrderStatus status) {
        return new Order(
                id,
                station,
                vehicle,
                driver,
                OffsetDateTime.parse("2026-06-28T09:00:00+07:00"),
                OffsetDateTime.parse("2026-06-28T11:00:00+07:00"),
                new BigDecimal("2.00"),
                station.getPricePerHour(),
                50000,
                5000,
                55000,
                status);
    }
}
