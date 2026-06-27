package com.app.venus.modules.advisor.interfaces.rest;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.app.venus.modules.advisor.application.AdvisorChatProvider;
import com.app.venus.modules.advisor.application.AdvisorContract;
import com.app.venus.modules.advisor.application.VolzenAdvisorService;
import com.app.venus.modules.advisor.domain.AdvisorProvider;
import com.app.venus.modules.advisor.infrastructure.AppAdvisorProperties;
import com.app.venus.modules.advisor.interfaces.dto.request.AdvisorChatRequest;
import com.app.venus.modules.advisor.interfaces.dto.response.AdvisorChatResponse;

import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
@AutoConfigureMockMvc
class AdvisorControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void validQuestionReturnsAdvisorResponse() throws Exception {
        mockMvc.perform(post(AdvisorContract.CHAT_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"message":"Does Volzen sell electricity?"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("openai"))
                .andExpect(jsonPath("$.grounded").value(true))
                .andExpect(jsonPath("$.sourceIds[0]").value("VOLZEN-POLICY-001"))
                .andExpect(jsonPath("$", not(hasKey("rawPrompt"))))
                .andExpect(jsonPath("$", not(hasKey("apiKey"))));
    }

    @Test
    void locationQuestionReturnsLocationSource() throws Exception {
        mockMvc.perform(post(AdvisorContract.CHAT_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "message":"What is District 7 demand potential?",
                          "locationContext":{"district":"District 7","city":"Ho Chi Minh City"}
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer", containsString("District 7")))
                .andExpect(jsonPath("$.sourceIds[0]").value("VOLZEN-LOCATION-SIGNALS-001"));
    }

    @Test
    void unsupportedQuestionReturnsFallback() throws Exception {
        mockMvc.perform(post(AdvisorContract.CHAT_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"message":"Write a bedtime story."}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grounded").value(false))
                .andExpect(jsonPath("$.answer").value(AdvisorContract.FALLBACK_ANSWER));
    }

    @Test
    void validationFailureUsesProductErrorShape() throws Exception {
        mockMvc.perform(post(AdvisorContract.CHAT_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"message":""}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    @Test
    void providerFailureReturnsFallback() throws Exception {
        mockMvc.perform(post(AdvisorContract.CHAT_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"message":"trigger provider failure"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grounded").value(false))
                .andExpect(jsonPath("$.answer").value(AdvisorContract.FALLBACK_ANSWER));
    }

    @Test
    void existingGenericAiRoutesStayAvailable() throws Exception {
        mockMvc.perform(get("/api/ai/status"))
                .andExpect(status().isOk());
    }

    @TestConfiguration
    static class AdvisorControllerTestConfig {
        @Bean
        @Primary
        VolzenAdvisorService testAdvisorService() {
            return new VolzenAdvisorService(new AppAdvisorProperties(), List.of(new AdvisorChatProvider() {
                @Override
                public AdvisorProvider provider() {
                    return AdvisorProvider.OPENAI;
                }

                @Override
                public AdvisorChatResponse chat(AdvisorChatRequest request) {
                    if (request.message().contains("failure") || request.message().contains("bedtime")) {
                        return response(AdvisorContract.FALLBACK_ANSWER, List.of(), false);
                    }
                    if (request.locationContext() != null) {
                        return response(
                                "District 7 has proxy demand potential.",
                                List.of("VOLZEN-LOCATION-SIGNALS-001"),
                                true);
                    }
                    return response(
                            "Volzen does not sell electricity.",
                            List.of("VOLZEN-POLICY-001"),
                            true);
                }

                private AdvisorChatResponse response(String answer, List<String> sourceIds, boolean grounded) {
                    return new AdvisorChatResponse(
                            answer,
                            sourceIds,
                            List.of(),
                            grounded,
                            false,
                            LocalDate.parse("2026-06-28"),
                            AdvisorProvider.OPENAI,
                            grounded ? null : AdvisorContract.FALLBACK_ANSWER);
                }
            }));
        }
    }
}
