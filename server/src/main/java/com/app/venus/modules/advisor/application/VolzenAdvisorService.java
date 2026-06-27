package com.app.venus.modules.advisor.application;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.venus.modules.advisor.domain.AdvisorProvider;
import com.app.venus.modules.advisor.infrastructure.AppAdvisorProperties;
import com.app.venus.modules.advisor.interfaces.dto.request.AdvisorChatRequest;
import com.app.venus.modules.advisor.interfaces.dto.response.AdvisorChatResponse;

@Service
public class VolzenAdvisorService {
    private final AppAdvisorProperties properties;
    private final Map<AdvisorProvider, AdvisorChatProvider> providers;

    public VolzenAdvisorService(AppAdvisorProperties properties, List<AdvisorChatProvider> providers) {
        this.properties = properties;
        this.providers = providers.stream()
                .collect(Collectors.toUnmodifiableMap(AdvisorChatProvider::provider, Function.identity()));
    }

    public AdvisorChatResponse chat(AdvisorChatRequest request) {
        AdvisorProvider provider = selectProvider(request);
        AdvisorChatProvider chatProvider = providers.getOrDefault(provider, providers.get(AdvisorProvider.OPENAI));
        if (chatProvider == null) {
            chatProvider = providers.values().stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("No advisor providers are registered."));
        }

        AdvisorChatResponse response = chatProvider.chat(request);
        return normalizeResponse(response);
    }

    private AdvisorProvider selectProvider(AdvisorChatRequest request) {
        if (properties.isRequestProviderOverrideEnabled() && request.preferredProvider() != null) {
            return request.preferredProvider();
        }
        return configuredProvider();
    }

    private AdvisorProvider configuredProvider() {
        String value = properties.getProvider();
        if (value == null || value.isBlank()) {
            return AdvisorProvider.OPENAI;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "ollama" -> AdvisorProvider.OLLAMA;
            case "mock" -> AdvisorProvider.MOCK;
            default -> AdvisorProvider.OPENAI;
        };
    }

    private AdvisorChatResponse normalizeResponse(AdvisorChatResponse response) {
        boolean needsProfessionalReview = response.needsProfessionalReview()
                || touchesProfessionalReviewTopic(response.answer());
        boolean grounded = response.grounded() && response.sourceIds() != null && !response.sourceIds().isEmpty();
        if (!grounded) {
            return new AdvisorChatResponse(
                    AdvisorContract.FALLBACK_ANSWER,
                    List.of(),
                    response.retrievedSources(),
                    false,
                    needsProfessionalReview,
                    response.dataAsOf(),
                    response.provider(),
                    AdvisorContract.FALLBACK_ANSWER);
        }
        return new AdvisorChatResponse(
                response.answer(),
                response.sourceIds(),
                response.retrievedSources(),
                true,
                needsProfessionalReview,
                response.dataAsOf(),
                response.provider(),
                response.unsupportedReason());
    }

    private boolean touchesProfessionalReviewTopic(String answer) {
        if (answer == null) {
            return false;
        }
        String value = answer.toLowerCase(Locale.ROOT);
        return value.contains("legal")
                || value.contains("law")
                || value.contains("permit")
                || value.contains("electrical")
                || value.contains("safety")
                || value.contains("technical review")
                || value.contains("professional review")
                || value.contains("profit")
                || value.contains("demand")
                || value.contains("readiness");
    }
}
