package com.app.venus.modules.advisor.interfaces.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.app.venus.modules.advisor.domain.AdvisorProvider;
import com.app.venus.modules.advisor.interfaces.dto.request.AdvisorChatRequest;
import com.app.venus.modules.advisor.interfaces.dto.request.AdvisorLocationContextRequest;
import com.app.venus.modules.advisor.interfaces.dto.response.AdvisorChatResponse;
import com.app.venus.modules.advisor.interfaces.dto.response.AdvisorRetrievedSourceResponse;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import tools.jackson.databind.ObjectMapper;

class AdvisorDtoSerializationTests {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void requestShapeSerializesWithLowercaseProvider() throws Exception {
        AdvisorChatRequest request = new AdvisorChatRequest(
                "Should I host a charger in District 1?",
                "conv_demo",
                new AdvisorLocationContextRequest(
                        "District 1",
                        "Ho Chi Minh City",
                        "Nguyen Hue",
                        new BigDecimal("10.7758"),
                        new BigDecimal("106.7009"),
                        6,
                        List.of("office", "mall"),
                        "medium",
                        "high proxy potential",
                        "hcmc-d1-office-core"),
                AdvisorProvider.OPENAI);

        String json = objectMapper.writeValueAsString(request);

        assertThat(json).contains("\"message\":\"Should I host a charger in District 1?\"");
        assertThat(json).contains("\"preferredProvider\":\"openai\"");
        assertThat(json).contains("\"curatedLocationId\":\"hcmc-d1-office-core\"");
    }

    @Test
    void responseShapeSerializesSourcesAndProvider() throws Exception {
        AdvisorChatResponse response = new AdvisorChatResponse(
                "Use this as a proxy signal, then verify the site.",
                List.of("VOLZEN-PILOT-001"),
                List.of(new AdvisorRetrievedSourceResponse(
                        "VOLZEN-PILOT-001",
                        "Long-stay parking is a proxy demand signal.",
                        "Volzen MVP location-demand proxy assumptions",
                        "internal",
                        "internal/pilot",
                        LocalDate.parse("2026-06-28"))),
                true,
                true,
                LocalDate.parse("2026-06-28"),
                AdvisorProvider.MOCK,
                null);

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"provider\":\"mock\"");
        assertThat(json).contains("\"sourceIds\":[\"VOLZEN-PILOT-001\"]");
        assertThat(json).contains("\"retrievedSources\"");
    }

    @Test
    void requestValidationRejectsBlankMessageAndInvalidCoordinates() {
        AdvisorChatRequest request = new AdvisorChatRequest(
                "",
                null,
                new AdvisorLocationContextRequest(
                        null,
                        null,
                        null,
                        new BigDecimal("91.0"),
                        new BigDecimal("181.0"),
                        -1,
                        null,
                        null,
                        null,
                        null),
                null);

        Set<?> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(4);
    }
}
