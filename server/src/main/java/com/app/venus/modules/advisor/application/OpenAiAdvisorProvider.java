package com.app.venus.modules.advisor.application;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.venus.modules.advisor.domain.AdvisorProvider;
import com.app.venus.modules.advisor.infrastructure.AppAdvisorProperties;
import com.app.venus.modules.advisor.infrastructure.OpenAiAdvisorModelClient;
import com.app.venus.modules.advisor.interfaces.dto.request.AdvisorChatRequest;
import com.app.venus.modules.advisor.interfaces.dto.response.AdvisorChatResponse;
import com.app.venus.modules.advisor.interfaces.dto.response.AdvisorRetrievedSourceResponse;

@Service
public class OpenAiAdvisorProvider implements AdvisorChatProvider {
    private final AppAdvisorProperties properties;
    private final AdvisorKnowledgeBase knowledgeBase;
    private final AdvisorGuardrailPromptBuilder guardrailPromptBuilder;
    private final AdvisorModelClient modelClient;
    private final AdvisorProviderOutputValidator outputValidator;

    @Autowired
    public OpenAiAdvisorProvider(
            AppAdvisorProperties properties,
            AdvisorKnowledgeBase knowledgeBase,
            AdvisorGuardrailPromptBuilder guardrailPromptBuilder,
            OpenAiAdvisorModelClient modelClient) {
        this(properties, knowledgeBase, guardrailPromptBuilder, modelClient, new AdvisorProviderOutputValidator());
    }

    OpenAiAdvisorProvider(
            AppAdvisorProperties properties,
            AdvisorKnowledgeBase knowledgeBase,
            AdvisorGuardrailPromptBuilder guardrailPromptBuilder,
            AdvisorModelClient modelClient,
            AdvisorProviderOutputValidator outputValidator) {
        this.properties = properties;
        this.knowledgeBase = knowledgeBase;
        this.guardrailPromptBuilder = guardrailPromptBuilder;
        this.modelClient = modelClient;
        this.outputValidator = outputValidator;
    }

    @Override
    public AdvisorProvider provider() {
        return AdvisorProvider.OPENAI;
    }

    @Override
    public AdvisorChatResponse chat(AdvisorChatRequest request) {
        AdvisorRetrievalResult retrieval = knowledgeBase.retrieve(request.message(), request.locationContext(), 6);
        if (!retrieval.supported()) {
            return fallbackResponse(retrieval.snippets(), AdvisorContract.FALLBACK_ANSWER);
        }

        String systemPrompt = guardrailPromptBuilder.build(retrieval.snippets());
        String userPrompt = userPrompt(request, retrieval);

        try {
            return outputValidator.validate(
                    modelClient.completeJson(userPrompt, systemPrompt),
                    retrieval.snippets(),
                    AdvisorProvider.OPENAI);
        } catch (RuntimeException firstFailure) {
            return retryOrFallback(request, retrieval, systemPrompt, firstFailure);
        }
    }

    private AdvisorChatResponse retryOrFallback(
            AdvisorChatRequest request,
            AdvisorRetrievalResult retrieval,
            String systemPrompt,
            RuntimeException firstFailure) {
        try {
            String strictPrompt = userPrompt(request, retrieval)
                    + "\nReturn only valid JSON. Use only retrieved sourceIds. Do not add markdown.";
            return outputValidator.validate(
                    modelClient.completeJson(strictPrompt, systemPrompt),
                    retrieval.snippets(),
                    AdvisorProvider.OPENAI);
        } catch (RuntimeException secondFailure) {
            return AdvisorGroundedFallbackComposer.compose(
                    retrieval.snippets(),
                    AdvisorProvider.OPENAI,
                    AdvisorContract.FALLBACK_ANSWER);
        }
    }

    private String userPrompt(AdvisorChatRequest request, AdvisorRetrievalResult retrieval) {
        String retrievalMode = properties.hasOpenAiVectorStore()
                ? "OpenAI hosted retrieval may be used with vectorStoreId configured on the server."
                : "OpenAI hosted retrieval is not configured; use the provided local retrieved snippets.";
        return """
                Question: %s
                Provider mode: %s
                Preferred model: %s
                Output fields: answer, sourceIds, grounded, needsProfessionalReview, dataAsOf, provider, unsupportedReason.
                Retrieved source count: %d
                """.formatted(
                request.message(),
                retrievalMode,
                properties.getOpenai().getModel(),
                retrieval.snippets().size());
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
                AdvisorProvider.OPENAI,
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
