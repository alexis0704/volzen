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

        assertThat(response.grounded()).isFalse();
        assertThat(response.answer()).isEqualTo(AdvisorContract.FALLBACK_ANSWER);
        assertThat(response.unsupportedReason()).isEqualTo(AdvisorContract.FALLBACK_ANSWER);
        assertThat(client.calls).isEqualTo(2);
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
