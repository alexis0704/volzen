package com.app.venus.modules.advisor.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import org.junit.jupiter.api.Test;

import com.app.venus.modules.advisor.domain.AdvisorProvider;
import com.app.venus.modules.advisor.interfaces.dto.request.AdvisorChatRequest;

class OllamaAdvisorProviderTests {
    private final AdvisorKnowledgeBase knowledgeBase = new AdvisorKnowledgeBase(List.of(
            read("product-boundaries.md"),
            read("vietnam-ev-law.md"),
            read("vietnam-ev-market.md"),
            read("location-signals.json")));

    @Test
    void returnsValidatedOllamaResponse() {
        FakeAdvisorModelClient client = new FakeAdvisorModelClient("""
                {"answer":"Connector choice should start with target driver vehicles and technical review.","sourceIds":["VOLZEN-PILOT-003"],"grounded":true,"needsProfessionalReview":true,"dataAsOf":"2026-06-28","provider":"ollama","unsupportedReason":null}
                """);

        var response = provider(client).chat(new AdvisorChatRequest("Which connector should we recommend?", null, null, null));

        assertThat(response.provider()).isEqualTo(AdvisorProvider.OLLAMA);
        assertThat(response.grounded()).isTrue();
        assertThat(response.sourceIds()).containsExactly("VOLZEN-PILOT-003");
        assertThat(client.lastPrompt).contains("local/demo Ollama retrieval only");
    }

    @Test
    void retriesMalformedOutputOnce() {
        FakeAdvisorModelClient client = new FakeAdvisorModelClient(
                "malformed",
                """
                        {"answer":"Operators remain responsible for metering and safety.","sourceIds":["VOLZEN-POLICY-002"],"grounded":true,"needsProfessionalReview":true,"dataAsOf":"2026-06-28","provider":"ollama","unsupportedReason":null}
                        """);

        var response = provider(client).chat(new AdvisorChatRequest("Who owns metering and safety responsibility?", null, null, null));

        assertThat(response.provider()).isEqualTo(AdvisorProvider.OLLAMA);
        assertThat(response.sourceIds()).containsExactly("VOLZEN-POLICY-002");
        assertThat(client.calls).isEqualTo(2);
    }

    @Test
    void timeoutReturnsSafeFallback() {
        FakeAdvisorModelClient client = new FakeAdvisorModelClient(new RuntimeException("timeout"));

        var response = provider(client).chat(new AdvisorChatRequest("How should hotel onboarding work?", null, null, null));

        assertThat(response.provider()).isEqualTo(AdvisorProvider.OLLAMA);
        assertThat(response.grounded()).isFalse();
        assertThat(response.answer()).isEqualTo(AdvisorContract.FALLBACK_ANSWER);
        assertThat(client.calls).isEqualTo(2);
    }

    @Test
    void unsupportedQuestionDoesNotCallOllama() {
        FakeAdvisorModelClient client = new FakeAdvisorModelClient("""
                {"answer":"unused","sourceIds":[],"grounded":false,"needsProfessionalReview":false,"dataAsOf":"2026-06-28","provider":"ollama","unsupportedReason":"unused"}
                """);

        var response = provider(client).chat(new AdvisorChatRequest("Tell me a bedtime story.", null, null, null));

        assertThat(response.provider()).isEqualTo(AdvisorProvider.OLLAMA);
        assertThat(response.grounded()).isFalse();
        assertThat(response.answer()).isEqualTo(AdvisorContract.FALLBACK_ANSWER);
        assertThat(client.calls).isZero();
    }

    private OllamaAdvisorProvider provider(AdvisorModelClient client) {
        return new OllamaAdvisorProvider(
                knowledgeBase,
                new AdvisorGuardrailPromptBuilder(),
                client,
                new AdvisorProviderOutputValidator());
    }

    private static AdvisorKnowledgeBase.NamedContent read(String name) {
        try (var stream = OllamaAdvisorProviderTests.class.getResourceAsStream("/advisor/knowledge/" + name)) {
            return new AdvisorKnowledgeBase.NamedContent(name, new String(stream.readAllBytes()));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static class FakeAdvisorModelClient implements AdvisorModelClient {
        private final Queue<Object> results = new ArrayDeque<>();
        private int calls;
        private String lastPrompt;

        FakeAdvisorModelClient(Object... results) {
            this.results.addAll(List.of(results));
        }

        @Override
        public String completeJson(String prompt, String systemPrompt) {
            calls++;
            lastPrompt = prompt;
            Object result = results.peek() == null ? "" : results.poll();
            if (result instanceof RuntimeException exception) {
                throw exception;
            }
            return result.toString();
        }

        @Override
        public String providerName() {
            return "ollama";
        }
    }
}
