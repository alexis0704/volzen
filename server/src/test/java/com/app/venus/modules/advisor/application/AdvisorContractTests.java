package com.app.venus.modules.advisor.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AdvisorContractTests {
    @Test
    void definesMvpChatEndpoint() {
        assertThat(AdvisorContract.CHAT_ENDPOINT).isEqualTo("/api/v1/advisor/chat");
    }

    @Test
    void definesRequestLocationAndResponseFields() {
        assertThat(AdvisorContract.REQUEST_FIELDS)
                .containsExactly("message", "conversationId", "locationContext", "preferredProvider");
        assertThat(AdvisorContract.LOCATION_CONTEXT_FIELDS)
                .containsExactly(
                        "district",
                        "city",
                        "address",
                        "lat",
                        "lng",
                        "nearbyChargerCount",
                        "siteTypeSignals",
                        "longStayParkingPotential",
                        "demandPotentialLabel",
                        "curatedLocationId");
        assertThat(AdvisorContract.RESPONSE_FIELDS)
                .containsExactly(
                        "answer",
                        "sourceIds",
                        "grounded",
                        "needsProfessionalReview",
                        "dataAsOf",
                        "provider",
                        "unsupportedReason");
    }

    @Test
    void definesShortGroundedAnswerRulesAndFallback() {
        assertThat(AdvisorContract.MIN_ANSWER_SENTENCES).isEqualTo(2);
        assertThat(AdvisorContract.MAX_ANSWER_SENTENCES).isEqualTo(4);
        assertThat(AdvisorContract.TARGET_MAX_WORDS).isEqualTo(75);
        assertThat(AdvisorContract.FALLBACK_ANSWER)
                .isEqualTo("I don't have a verified answer for that yet.");
        assertThat(AdvisorContract.ANSWER_RULES)
                .anySatisfy(rule -> assertThat(rule).contains("sourceIds"))
                .anySatisfy(rule -> assertThat(rule).contains("grounded=false"))
                .anySatisfy(rule -> assertThat(rule).contains("informational only"))
                .anySatisfy(rule -> assertThat(rule).contains("charging-demand potential"))
                .anySatisfy(rule -> assertThat(rule).contains("proxy estimate"))
                .anySatisfy(rule -> assertThat(rule).contains("Exact EV-driver counts"));
    }

    @Test
    void documentsUnsupportedTopics() {
        assertThat(AdvisorContract.UNSUPPORTED_TOPICS)
                .containsExactly(
                        "general chat",
                        "legal conclusions",
                        "electrical certifications",
                        "exact profitability forecasts",
                        "real-time charger availability",
                        "unverified district EV-user counts");
    }
}
