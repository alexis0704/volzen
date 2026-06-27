package com.app.venus.modules.advisor.application;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.venus.modules.advisor.domain.AdvisorProvider;
import com.app.venus.modules.advisor.infrastructure.OllamaAdvisorModelClient;
import com.app.venus.modules.advisor.interfaces.dto.request.AdvisorChatRequest;
import com.app.venus.modules.advisor.interfaces.dto.response.AdvisorChatResponse;
import com.app.venus.modules.advisor.interfaces.dto.response.AdvisorRetrievedSourceResponse;

@Service
public class OllamaAdvisorProvider implements AdvisorChatProvider {
    private final AdvisorKnowledgeBase knowledgeBase;
    private final AdvisorGuardrailPromptBuilder guardrailPromptBuilder;
    private final AdvisorModelClient modelClient;
    private final AdvisorProviderOutputValidator outputValidator;

    @Autowired
    public OllamaAdvisorProvider(
            AdvisorKnowledgeBase knowledgeBase,
            AdvisorGuardrailPromptBuilder guardrailPromptBuilder,
            OllamaAdvisorModelClient modelClient) {
        this(knowledgeBase, guardrailPromptBuilder, modelClient, new AdvisorProviderOutputValidator());
    }

    OllamaAdvisorProvider(
            AdvisorKnowledgeBase knowledgeBase,
            AdvisorGuardrailPromptBuilder guardrailPromptBuilder,
            AdvisorModelClient modelClient,
            AdvisorProviderOutputValidator outputValidator) {
        this.knowledgeBase = knowledgeBase;
        this.guardrailPromptBuilder = guardrailPromptBuilder;
        this.modelClient = modelClient;
        this.outputValidator = outputValidator;
    }

    @Override
    public AdvisorProvider provider() {
        return AdvisorProvider.OLLAMA;
    }

    @Override
    public AdvisorChatResponse chat(AdvisorChatRequest request) {
        AdvisorRetrievalResult retrieval = knowledgeBase.retrieve(request.message(), request.locationContext(), 5);
        if (!retrieval.supported()) {
            return fallbackResponse(retrieval.snippets(), AdvisorContract.FALLBACK_ANSWER);
        }

        String systemPrompt = guardrailPromptBuilder.build(retrieval.snippets());
        String prompt = userPrompt(request, retrieval);
        try {
            return outputValidator.validate(
                    modelClient.completeJson(prompt, systemPrompt),
                    retrieval.snippets(),
                    AdvisorProvider.OLLAMA);
        } catch (RuntimeException firstFailure) {
            return retryOrFallback(request, retrieval, systemPrompt);
        }
    }

    private AdvisorChatResponse retryOrFallback(
            AdvisorChatRequest request,
            AdvisorRetrievalResult retrieval,
            String systemPrompt) {
        try {
            String strictPrompt = userPrompt(request, retrieval)
                    + "\nReturn only valid JSON. Use only sourceIds from local retrieved snippets. Do not add markdown.";
            return outputValidator.validate(
                    modelClient.completeJson(strictPrompt, systemPrompt),
                    retrieval.snippets(),
                    AdvisorProvider.OLLAMA);
        } catch (RuntimeException secondFailure) {
            return fallbackResponse(retrieval.snippets(), AdvisorContract.FALLBACK_ANSWER);
        }
    }

    private String userPrompt(AdvisorChatRequest request, AdvisorRetrievalResult retrieval) {
        return """
                Question: %s
                Provider mode: local/demo Ollama retrieval only; do not claim live web knowledge.
                Output fields: answer, sourceIds, grounded, needsProfessionalReview, dataAsOf, provider, unsupportedReason.
                Use only the %d top local snippets supplied in the system prompt.
                """.formatted(request.message(), retrieval.snippets().size());
    }

    private AdvisorChatResponse fallbackResponse(List<AdvisorKnowledgeSnippet> snippets, String reason) {
        LocalDate dataAsOf = snippets.stream()
                .map(AdvisorKnowledgeSnippet::dataAsOf)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());
        return new AdvisorChatResponse(
                AdvisorContract.FALLBACK_ANSWER,
                List.of(),
                snippets.stream().map(this::toRetrievedSource).toList(),
                false,
                false,
                dataAsOf,
                AdvisorProvider.OLLAMA,
                reason);
    }

    private AdvisorRetrievedSourceResponse toRetrievedSource(AdvisorKnowledgeSnippet snippet) {
        return new AdvisorRetrievedSourceResponse(
                snippet.sourceId(),
                snippet.claim(),
                snippet.source().title(),
                snippet.source().url(),
                snippet.source().sourceType(),
                snippet.dataAsOf());
    }
}
