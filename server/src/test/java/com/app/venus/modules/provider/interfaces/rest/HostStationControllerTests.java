package com.app.venus.modules.provider.interfaces.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.app.venus.modules.order.infrastructure.OrderRepository;
import com.app.venus.modules.provider.domain.Station;
import com.app.venus.modules.provider.infrastructure.StationRepository;
import com.app.venus.modules.review.infrastructure.ReviewRepository;
import com.app.venus.modules.user.application.DemoCurrentUserService;
import com.app.venus.modules.user.domain.User;
import com.app.venus.modules.user.infrastructure.UserRepository;
import com.app.venus.modules.vehicle.infrastructure.VehicleRepository;
import com.app.venus.shared.domain.Amenity;
import com.app.venus.shared.domain.ConnectorType;

@SpringBootTest
@AutoConfigureMockMvc
class HostStationControllerTests {
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

    @Autowired
    private DemoCurrentUserService demoCurrentUserService;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        orderRepository.deleteAll();
        vehicleRepository.deleteAll();
        stationRepository.deleteAll();
    }

    @Test
    void getsCurrentProviderStation() throws Exception {
        User provider = demoCurrentUserService.currentProvider();
        stationRepository.saveAndFlush(new Station(
                "pvd_host_station",
                provider,
                "Nguyen Hue Home Charger",
                "12 Nguyen Hue, District 1, Ho Chi Minh City",
                new BigDecimal("10.7769000"),
                new BigDecimal("106.7009000"),
                25000,
                Set.of(ConnectorType.CCS, ConnectorType.TYPE_2),
                Set.of(Amenity.COFFEE, Amenity.WIFI),
                List.of("https://cdn.volzen.vn/stations/pvd_host_station/photo_1.jpg"),
                true));

        mockMvc.perform(get("/api/v1/me/station"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("pvd_host_station"))
                .andExpect(jsonPath("$.name").value("Nguyen Hue Home Charger"))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.connectorTypes", hasSize(2)));
    }

    @Test
    void createsCurrentProviderStationWhenMissing() throws Exception {
        mockMvc.perform(put("/api/v1/me/station")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "New Host Charger",
                          "address": "1 Le Loi, District 1",
                          "lat": 10.7769,
                          "lng": 106.7009,
                          "pricePerHour": 42000,
                          "connectorTypes": ["CCS", "Type 2"],
                          "amenities": ["Coffee", "WiFi"],
                          "photoUrls": ["https://cdn.volzen.vn/stations/new/photo.jpg"],
                          "isAvailable": true
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("New Host Charger"))
                .andExpect(jsonPath("$.pricePerHour").value(42000))
                .andExpect(jsonPath("$.isAvailable").value(true))
                .andExpect(jsonPath("$.status").value("Active"));
    }

    @Test
    void updatesCurrentProviderStation() throws Exception {
        User provider = demoCurrentUserService.currentProvider();
        stationRepository.saveAndFlush(new Station(
                "pvd_host_update",
                provider,
                "Old Charger",
                "Old Address",
                new BigDecimal("10.7000000"),
                new BigDecimal("106.6000000"),
                20000,
                Set.of(ConnectorType.TYPE_2),
                Set.of(Amenity.PARKING),
                List.of(),
                false));

        mockMvc.perform(put("/api/v1/me/station")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Updated Host Charger",
                          "address": "99 Nguyen Hue",
                          "lat": 10.7769,
                          "lng": 106.7009,
                          "pricePerHour": 55000,
                          "connectorTypes": ["CCS"],
                          "amenities": ["Parking", "Security"],
                          "photoUrls": [],
                          "isAvailable": false
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("pvd_host_update"))
                .andExpect(jsonPath("$.name").value("Updated Host Charger"))
                .andExpect(jsonPath("$.pricePerHour").value(55000))
                .andExpect(jsonPath("$.isAvailable").value(false))
                .andExpect(jsonPath("$.status").value("Inactive"));
    }

    @Test
    void validationFailureUsesProductErrorShape() throws Exception {
        mockMvc.perform(put("/api/v1/me/station")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "",
                          "address": "",
                          "lat": 10.7769,
                          "lng": 106.7009,
                          "pricePerHour": 0,
                          "connectorTypes": []
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    @Test
    void invalidConnectorReturnsUnprocessableEntity() throws Exception {
        mockMvc.perform(put("/api/v1/me/station")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Bad Connector",
                          "address": "1 Le Loi",
                          "lat": 10.7769,
                          "lng": 106.7009,
                          "pricePerHour": 42000,
                          "connectorTypes": ["Magic Plug"]
                        }
                        """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }
}
