package com.app.venus.modules.vehicle.interfaces.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.app.venus.modules.user.application.DemoCurrentUserService;
import com.app.venus.modules.vehicle.application.VehicleService;
import com.app.venus.modules.vehicle.domain.Vehicle;
import com.app.venus.modules.vehicle.infrastructure.VehicleRepository;

@SpringBootTest
@AutoConfigureMockMvc
class VehicleControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private DemoCurrentUserService demoCurrentUserService;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();
        demoCurrentUserService.currentDriver();
    }

    @Test
    void createsAndListsVehicles() throws Exception {
        mockMvc.perform(post("/api/v1/me/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "brand": "VinFast",
                          "model": "VF8",
                          "year": 2024,
                          "connectorType": "CCS"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.brand").value("VinFast"))
                .andExpect(jsonPath("$.model").value("VF8"))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.connectorType").value("CCS"))
                .andExpect(jsonPath("$.isDefault").value(true));

        mockMvc.perform(get("/api/v1/me/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicles", hasSize(1)))
                .andExpect(jsonPath("$.vehicles[0].brand").value("VinFast"));
    }

    @Test
    void updatesVehicleAndSwitchesDefault() throws Exception {
        Vehicle first = vehicleService.createCurrentDriverVehicle("VinFast", "VF8", 2024, "CCS");
        Vehicle second = vehicleService.createCurrentDriverVehicle("Toyota", "bZ4X", 2023, "Type 2");

        mockMvc.perform(patch("/api/v1/me/vehicles/{vehicleId}", second.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "brand": "Toyota",
                          "model": "bZ4X AWD",
                          "year": 2025,
                          "connectorType": "Type 2",
                          "isDefault": true
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("bZ4X AWD"))
                .andExpect(jsonPath("$.year").value(2025))
                .andExpect(jsonPath("$.isDefault").value(true));

        mockMvc.perform(get("/api/v1/me/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicles[0].id").value(second.getId()))
                .andExpect(jsonPath("$.vehicles[0].isDefault").value(true))
                .andExpect(jsonPath("$.vehicles[1].id").value(first.getId()))
                .andExpect(jsonPath("$.vehicles[1].isDefault").value(false));
    }

    @Test
    void deletesVehicleWhenMoreThanOneExists() throws Exception {
        Vehicle first = vehicleService.createCurrentDriverVehicle("VinFast", "VF8", 2024, "CCS");
        vehicleService.createCurrentDriverVehicle("Toyota", "bZ4X", 2023, "Type 2");

        mockMvc.perform(delete("/api/v1/me/vehicles/{vehicleId}", first.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/me/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicles", hasSize(1)))
                .andExpect(jsonPath("$.vehicles[0].isDefault").value(true));
    }

    @Test
    void deletingOnlyVehicleReturnsConflict() throws Exception {
        Vehicle vehicle = vehicleService.createCurrentDriverVehicle("VinFast", "VF8", 2024, "CCS");

        mockMvc.perform(delete("/api/v1/me/vehicles/{vehicleId}", vehicle.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("RESOURCE_CONFLICT"))
                .andExpect(jsonPath("$.message").value("Cannot delete the only vehicle on the account."));
    }

    @Test
    void invalidVehicleRequestReturnsProductValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/me/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "brand": "",
                          "model": "VF8",
                          "year": 2024,
                          "connectorType": "CCS"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }
}
