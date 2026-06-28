package com.app.venus.modules.advisor.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import org.junit.jupiter.api.Test;

import com.app.venus.modules.advisor.domain.AdvisorProvider;
import com.app.venus.modules.advisor.infrastructure.AppAdvisorProperties;
import com.app.venus.modules.advisor.interfaces.dto.request.AdvisorChatRequest;

class OpenAiAdvisorProviderTests {
    private final AdvisorKnowledgeBase knowledgeBase = new AdvisorKnowledgeBase(List.of(
            read("product-boundaries.md"),
            read("vietnam-ev-law.md"),
            read("vietnam-ev-market.md"),
            read("location-signals.json")));

    @Test
    void returnsValidatedOpenAiResponse() {
        FakeAdvisorModelClient client = new FakeAdvisorModelClient("""
                {"answer":"Volzen does not sell electricity; operators remain responsible for compliance.","sourceIds":["VOLZEN-POLICY-001"],"grounded":true,"needsProfessionalReview":false,"dataAsOf":"2026-06-28","provider":"openai","unsupportedReason":null}
                """);
        OpenAiAdvisorProvider provider = provider(client);

        var response = provider.chat(new AdvisorChatRequest("Does Volzen sell electricity?", null, null, null));

        assertThat(response.provider()).isEqualTo(AdvisorProvider.OPENAI);
        assertThat(response.grounded()).isTrue();
        assertThat(response.sourceIds()).containsExactly("VOLZEN-POLICY-001");
        assertThat(client.calls).isEqualTo(1);
    }

    @Test
    void retriesInvalidJsonOnceWithStricterFormatting() {
        FakeAdvisorModelClient client = new FakeAdvisorModelClient(
                "not json",
                """
                        {"answer":"Use District 7 as a proxy signal, then verify the site.","sourceIds":["VOLZEN-LOCATION-SIGNALS-001"],"grounded":true,"needsProfessionalReview":true,"dataAsOf":"2026-06-28","provider":"openai","unsupportedReason":null}
                        """);
        OpenAiAdvisorProvider provider = provider(client);

        var response = provider.chat(new AdvisorChatRequest("What is District 7 demand potential?", null, null, null));

        assertThat(response.grounded()).isTrue();
        assertThat(response.sourceIds()).containsExactly("VOLZEN-LOCATION-SIGNALS-001");
        assertThat(client.calls).isEqualTo(2);
    }

    @Test
    void timeoutReturnsSafeFallback() {
        FakeAdvisorModelClient client = new FakeAdvisorModelClient(new RuntimeException("timeout"));
        OpenAiAdvisorProvider provider = provider(client);

        var response = provider.chat(new AdvisorChatRequest("Which connector should we recommend?", null, null, null));

        assertThat(response.grounded()).isTrue();
        assertThat(response.answer()).contains("Connector choice should be based on the vehicles");
        assertThat(response.sourceIds()).contains("VOLZEN-PILOT-003");
        assertThat(response.unsupportedReason()).isEqualTo(AdvisorContract.FALLBACK_ANSWER);
        assertThat(client.calls).isEqualTo(2);
    }

    @Test
    void inventedHostUrlsReturnSafeFallback() {
        FakeAdvisorModelClient client = new FakeAdvisorModelClient("""
                {"answer":"Become a host at https://www.volzen.com/hosts.","sourceIds":["VOLZEN-POLICY-006"],"grounded":true,"needsProfessionalReview":false,"dataAsOf":"2026-06-28","provider":"openai","unsupportedReason":null}
                """);
        OpenAiAdvisorProvider provider = provider(client);

        var response = provider.chat(new AdvisorChatRequest("How do I become a host?", null, null, null));

        assertThat(response.grounded()).isTrue();
        assertThat(response.answer()).contains("use the in-app provider onboarding flow");
        assertThat(response.answer()).doesNotContain("https://www.volzen.com/hosts");
        assertThat(response.sourceIds()).contains("VOLZEN-POLICY-006");
        assertThat(client.calls).isEqualTo(2);
    }

    @Test
    void invalidLegalModelOutputFallsBackToGroundedKnowledge() {
        FakeAdvisorModelClient client = new FakeAdvisorModelClient("not json");
        OpenAiAdvisorProvider provider = provider(client);

        var response = provider.chat(new AdvisorChatRequest("Is selling electricity illegal in Vietnam?", null, null, null));

        assertThat(response.grounded()).isTrue();
        assertThat(response.answer()).contains("I cannot say selling electricity is illegal as legal advice");
        assertThat(response.answer()).contains("retail electricity pricing is regulated in Vietnam");
        assertThat(response.sourceIds()).contains("LAW-01");
        assertThat(response.needsProfessionalReview()).isTrue();
        assertThat(client.calls).isEqualTo(2);
    }

    @Test
    void invalidLocationModelOutputFallsBackToReadableDemandAnswer() {
        FakeAdvisorModelClient client = new FakeAdvisorModelClient("not json");
        OpenAiAdvisorProvider provider = provider(client);

        var response = provider.chat(new AdvisorChatRequest("Does Vung Tau have high demand for renting EVs?", null, null, null));

        assertThat(response.grounded()).isTrue();
        assertThat(response.answer()).contains("Vung Tau");
        assertThat(response.answer()).contains("proxy signal");
        assertThat(response.answer()).doesNotContain("Ho Chi Minh City District 1");
        assertThat(response.sourceIds()).contains("VOLZEN-LOCATION-SIGNALS-001");
    }

    @Test
    void invalidPaymentModelOutputFallsBackToReadablePaymentAnswer() {
        FakeAdvisorModelClient client = new FakeAdvisorModelClient("not json");
        OpenAiAdvisorProvider provider = provider(client);

        var response = provider.chat(new AdvisorChatRequest("What payment methods are accepted?", null, null, null));

        assertThat(response.grounded()).isTrue();
        assertThat(response.answer()).contains("real payments are not connected yet");
        assertThat(response.answer()).contains("accepted payment methods are not verified");
        assertThat(response.sourceIds()).contains("VOLZEN-POLICY-007");
    }

    @Test
    void unsupportedQuestionDoesNotCallProvider() {
        FakeAdvisorModelClient client = new FakeAdvisorModelClient("""
                {"answer":"unused","sourceIds":[],"grounded":false,"needsProfessionalReview":false,"dataAsOf":"2026-06-28","provider":"openai","unsupportedReason":"unused"}
                """);
        OpenAiAdvisorProvider provider = provider(client);

        var response = provider.chat(new AdvisorChatRequest("Write a poem about lunch.", null, null, null));

        assertThat(response.grounded()).isFalse();
        assertThat(response.answer()).isEqualTo(AdvisorContract.FALLBACK_ANSWER);
        assertThat(client.calls).isZero();
    }

    private OpenAiAdvisorProvider provider(AdvisorModelClient client) {
        AppAdvisorProperties properties = new AppAdvisorProperties();
        properties.getOpenai().setModel("gpt-4.1-mini");
        return new OpenAiAdvisorProvider(
                properties,
                knowledgeBase,
                new AdvisorGuardrailPromptBuilder(),
                client,
                new AdvisorProviderOutputValidator());
    }

    private static AdvisorKnowledgeBase.NamedContent read(String name) {
        try (var stream = OpenAiAdvisorProviderTests.class.getResourceAsStream("/advisor/knowledge/" + name)) {
            return new AdvisorKnowledgeBase.NamedContent(name, new String(stream.readAllBytes()));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static class FakeAdvisorModelClient implements AdvisorModelClient {
        private final Queue<Object> results = new ArrayDeque<>();
        private int calls;

        FakeAdvisorModelClient(Object... results) {
            this.results.addAll(List.of(results));
        }

        @Override
        public String completeJson(String prompt, String systemPrompt) {
            calls++;
            Object result = results.peek() == null ? "" : results.poll();
            if (result instanceof RuntimeException exception) {
                throw exception;
            }
            return result.toString();
        }

        @Override
        public String providerName() {
            return "openai";
        }
    }
}
