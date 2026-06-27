package com.app.venus.modules.advisor.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.app.venus.modules.advisor.application.AdvisorKnowledgeBase.NamedContent;
import com.app.venus.modules.advisor.interfaces.dto.request.AdvisorLocationContextRequest;

class AdvisorKnowledgeBaseTests {
    private final AdvisorKnowledgeBase knowledgeBase = new AdvisorKnowledgeBase(List.of(
            read("product-boundaries.md"),
            read("vietnam-ev-law.md"),
            read("vietnam-ev-market.md"),
            read("location-signals.json")));

    @Test
    void parsesSourceMetadata() {
        assertThat(knowledgeBase.sources()).containsKey("VOLZEN-POLICY-001");
        assertThat(knowledgeBase.snippets()).extracting(AdvisorKnowledgeSnippet::sourceId)
                .contains("VOLZEN-PILOT-003", "VOLZEN-LOCATION-SIGNALS-001");
    }

    @Test
    void duplicateConflictingSourceMetadataFailsInitialization() {
        String content = """
                ## Knowledge Items
                - id: item
                  sourceId: DUP-1
                  claimType: internal/pilot
                  note: internal pilot item
                ## Sources
                - sourceId: DUP-1
                  title: One
                  url: internal
                  lastReviewed: 2026-06-28
                  sourceType: internal/pilot
                - sourceId: DUP-1
                  title: Two
                  url: internal
                  lastReviewed: 2026-06-28
                  sourceType: internal/pilot
                """;

        assertThatThrownBy(() -> new AdvisorKnowledgeBase(List.of(new NamedContent("bad.md", content))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate advisor sourceId");
    }

    @Test
    void retrievesElectricitySalesPolicy() {
        AdvisorRetrievalResult result = knowledgeBase.retrieve("Does Volzen sell electricity?", null, 4);

        assertThat(result.supported()).isTrue();
        assertThat(result.snippets()).extracting(AdvisorKnowledgeSnippet::sourceId)
                .contains("VOLZEN-POLICY-001");
    }

    @Test
    void retrievesHotelPartnerOnboarding() {
        AdvisorRetrievalResult result = knowledgeBase.retrieve("How should a hotel partner onboarding work?", null, 4);

        assertThat(result.supported()).isTrue();
        assertThat(result.snippets()).extracting(AdvisorKnowledgeSnippet::sourceId)
                .contains("VOLZEN-PILOT-002");
    }

    @Test
    void retrievesConnectorRecommendation() {
        AdvisorRetrievalResult result = knowledgeBase.retrieve("Which connector should we recommend?", null, 4);

        assertThat(result.supported()).isTrue();
        assertThat(result.snippets()).extracting(AdvisorKnowledgeSnippet::sourceId)
                .contains("VOLZEN-PILOT-003");
    }

    @Test
    void retrievesDistrictSevenDemandPotential() {
        AdvisorLocationContextRequest location = new AdvisorLocationContextRequest(
                "District 7", "Ho Chi Minh City", null, null, null, null, null, null, null, null);

        AdvisorRetrievalResult result = knowledgeBase.retrieve("What is District 7 demand potential?", location, 4);

        assertThat(result.supported()).isTrue();
        assertThat(result.snippets()).extracting(AdvisorKnowledgeSnippet::id)
                .contains("location-hcmc-d7-mixed-use");
    }

    @Test
    void retrievesMeteringAndSafetyResponsibility() {
        AdvisorRetrievalResult result = knowledgeBase.retrieve("Who owns metering and safety responsibility?", null, 4);

        assertThat(result.supported()).isTrue();
        assertThat(result.snippets()).extracting(AdvisorKnowledgeSnippet::sourceId)
                .contains("VOLZEN-POLICY-002");
    }

    @Test
    void unsupportedQuestionsDoNotProduceInventedSupport() {
        AdvisorRetrievalResult result = knowledgeBase.retrieve("Write a poem about lunch.", null, 4);

        assertThat(result.supported()).isFalse();
        assertThat(result.unsupportedReason()).isEqualTo(AdvisorContract.FALLBACK_ANSWER);
    }

    private static NamedContent read(String name) {
        try (var stream = AdvisorKnowledgeBaseTests.class.getResourceAsStream("/advisor/knowledge/" + name)) {
            return new NamedContent(name, new String(stream.readAllBytes()));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
