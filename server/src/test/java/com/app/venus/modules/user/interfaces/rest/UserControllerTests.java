package com.app.venus.modules.user.interfaces.rest;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCurrentUserReturnsDemoDriver() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("usr_demo_driver"))
                .andExpect(jsonPath("$.fullName").value("Demo Driver"))
                .andExpect(jsonPath("$.email").value("driver@volzen.test"))
                .andExpect(jsonPath("$.role").value("driver"))
                .andExpect(jsonPath("$.avatarUrl").value("https://cdn.volzen.vn/avatars/usr_demo_driver.jpg"))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    void patchCurrentUserUpdatesAllowedFields() throws Exception {
        mockMvc.perform(patch("/api/v1/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "fullName": "MVP Driver",
                          "avatarUrl": "https://cdn.volzen.vn/avatars/mvp-driver.jpg"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("usr_demo_driver"))
                .andExpect(jsonPath("$.fullName").value("MVP Driver"))
                .andExpect(jsonPath("$.avatarUrl").value("https://cdn.volzen.vn/avatars/mvp-driver.jpg"));
    }

    @Test
    void productValidationUsesProductErrorShape() throws Exception {
        mockMvc.perform(patch("/api/v1/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "fullName": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Validation failed."));
    }

    @Test
    void malformedProductRequestUsesProductErrorShape() throws Exception {
        mockMvc.perform(patch("/api/v1/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("REQUEST_BODY_INVALID"))
                .andExpect(jsonPath("$.message").value("Malformed request body."));
    }
}
