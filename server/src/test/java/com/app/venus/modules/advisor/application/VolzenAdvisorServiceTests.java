package com.app.venus.modules.advisor.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.app.venus.modules.advisor.domain.AdvisorProvider;
import com.app.venus.modules.advisor.infrastructure.AppAdvisorProperties;
import com.app.venus.modules.advisor.interfaces.dto.request.AdvisorChatRequest;
import com.app.venus.modules.advisor.interfaces.dto.request.AdvisorLocationContextRequest;
import com.app.venus.modules.advisor.interfaces.dto.response.AdvisorChatResponse;

class VolzenAdvisorServiceTests {
    private final AppAdvisorProperties properties = new AppAdvisorProperties();

    @Test
    void usesConfiguredOpenAiProviderForElectricitySalesBoundary() {
        VolzenAdvisorService service = service(response(
                "Volzen does not sell electricity.",
                List.of("VOLZEN-POLICY-001"),
                true,
                false));

        AdvisorChatResponse response = service.chat(request("Does Volzen sell electricity?"));

        assertThat(response.provider()).isEqualTo(AdvisorProvider.OPENAI);
        assertThat(response.grounded()).isTrue();
        assertThat(response.sourceIds()).containsExactly("VOLZEN-POLICY-001");
    }

    @Test
    void requestProviderOverrideOnlyWorksWhenEnabled() {
        properties.setRequestProviderOverrideEnabled(false);
        VolzenAdvisorService service = new VolzenAdvisorService(properties, List.of(
                new FixedProvider(AdvisorProvider.OPENAI, response("openai", List.of("VOLZEN-POLICY-001"), true, false)),
                new FixedProvider(AdvisorProvider.OLLAMA, response("ollama", List.of("VOLZEN-POLICY-001"), true, false))));

        AdvisorChatResponse response = service.chat(new AdvisorChatRequest(
                "Use Ollama",
                null,
                null,
                AdvisorProvider.OLLAMA));

        assertThat(response.provider()).isEqualTo(AdvisorProvider.OPENAI);
    }

    @Test
    void supportsHotelOnboardingParkingReadinessCompatibilityAndConnectorQuestions() {
        VolzenAdvisorService service = service(response(
                "Hotel onboarding should confirm parking permission, access rules, non-VinFast compatibility, and connector choice.",
                List.of("VOLZEN-PILOT-002", "VOLZEN-PILOT-003"),
                true,
                true));

        assertGrounded(service.chat(request("hotel onboarding")));
        assertGrounded(service.chat(request("parking-lot readiness")));
        assertGrounded(service.chat(request("non-VinFast compatibility")));
        assertGrounded(service.chat(request("connector recommendation")));
    }

    @Test
    void districtSevenDemandPotentialSetsProfessionalReviewFlag() {
        VolzenAdvisorService service = service(response(
                "District 7 has medium proxy demand potential; this is not a verified demand fact.",
                List.of("VOLZEN-LOCATION-SIGNALS-001"),
                true,
                false));

        AdvisorChatResponse response = service.chat(new AdvisorChatRequest(
                "District 7 demand potential",
                null,
                new AdvisorLocationContextRequest("District 7", "Ho Chi Minh City", null, null, null, null, null, null, null, null),
                null));

        assertThat(response.needsProfessionalReview()).isTrue();
        assertThat(response.answer()).contains("proxy demand");
    }

    @Test
    void unsupportedQuestionReturnsStandardFallback() {
        VolzenAdvisorService service = service(response(
                "No source",
                List.of(),
                false,
                false));

        AdvisorChatResponse response = service.chat(request("Write a song"));

        assertThat(response.grounded()).isFalse();
        assertThat(response.answer()).isEqualTo(AdvisorContract.FALLBACK_ANSWER);
        assertThat(response.unsupportedReason()).isEqualTo(AdvisorContract.FALLBACK_ANSWER);
    }

    @Test
    void professionalReviewFlagComputedForLegalAndSafetyLanguage() {
        VolzenAdvisorService service = service(response(
                "Legal and electrical safety readiness needs review.",
                List.of("VN-LAW-002"),
                true,
                false));

        AdvisorChatResponse response = service.chat(request("legal safety"));

        assertThat(response.needsProfessionalReview()).isTrue();
    }

    private VolzenAdvisorService service(AdvisorChatResponse response) {
        return new VolzenAdvisorService(properties, List.of(new FixedProvider(AdvisorProvider.OPENAI, response)));
    }

    private AdvisorChatRequest request(String message) {
        return new AdvisorChatRequest(message, null, null, null);
    }

    private AdvisorChatResponse response(String answer, List<String> sourceIds, boolean grounded, boolean review) {
        return new AdvisorChatResponse(
                answer,
                sourceIds,
                List.of(),
                grounded,
                review,
                LocalDate.parse("2026-06-28"),
                AdvisorProvider.OPENAI,
                grounded ? null : AdvisorContract.FALLBACK_ANSWER);
    }

    private void assertGrounded(AdvisorChatResponse response) {
        assertThat(response.grounded()).isTrue();
        assertThat(response.sourceIds()).isNotEmpty();
    }

    private record FixedProvider(AdvisorProvider provider, AdvisorChatResponse response) implements AdvisorChatProvider {
        @Override
        public AdvisorChatResponse chat(AdvisorChatRequest request) {
            return response;
        }
    }
}
