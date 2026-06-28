package com.app.venus.modules.advisor.application;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.app.venus.modules.advisor.domain.AdvisorProvider;
import com.app.venus.modules.advisor.interfaces.dto.response.AdvisorChatResponse;
import com.app.venus.modules.advisor.interfaces.dto.response.AdvisorRetrievedSourceResponse;

final class AdvisorGroundedFallbackComposer {
    private AdvisorGroundedFallbackComposer() {
    }

    static AdvisorChatResponse compose(List<AdvisorKnowledgeSnippet> snippets, AdvisorProvider provider, String reason) {
        if (snippets == null || snippets.isEmpty()) {
            return new AdvisorChatResponse(
                    AdvisorContract.FALLBACK_ANSWER,
                    List.of(),
                    List.of(),
                    false,
                    false,
                    LocalDate.now(),
                    provider,
                    reason);
        }

        List<AdvisorKnowledgeSnippet> topSnippets = snippets.stream().limit(3).toList();
        Set<String> sourceIds = new LinkedHashSet<>();
        topSnippets.forEach(snippet -> sourceIds.add(snippet.sourceId()));
        String answer = readableAnswer(topSnippets);

        return new AdvisorChatResponse(
                answer,
                List.copyOf(sourceIds),
                snippets.stream().map(AdvisorGroundedFallbackComposer::toRetrievedSource).toList(),
                true,
                needsProfessionalReview(topSnippets),
                topSnippets.stream()
                        .map(AdvisorKnowledgeSnippet::dataAsOf)
                        .max(LocalDate::compareTo)
                        .orElse(LocalDate.now()),
                provider,
                reason);
    }

    private static String readableAnswer(List<AdvisorKnowledgeSnippet> snippets) {
        String combined = combinedClaims(snippets);
        AdvisorKnowledgeSnippet first = snippets.get(0);
        String firstClaim = first.claim();
        String firstClaimLower = firstClaim.toLowerCase(Locale.ROOT);

        if (first.id().startsWith("location-")) {
            return locationAnswer(firstClaim);
        }
        if (first.id().contains("payment-methods")) {
            return "For the MVP demo, real payments are not connected yet. You may see booking totals or payment UI, but accepted payment methods are not verified until Volzen adds a payment-provider integration.";
        }
        if (first.id().contains("host-onboarding")) {
            return "To become a host in the MVP, use the in-app provider onboarding flow, prepare your business or property details, describe the charger and site, and wait for Volzen/manual review before the spot is treated as active.";
        }
        if (combined.contains("retail electricity pricing is regulated") || combined.contains("does not sell electricity")) {
            return "I cannot say selling electricity is illegal as legal advice. What the verified notes say is that retail electricity pricing is regulated in Vietnam, and Volzen should not present itself as the electricity seller unless a qualified operator and legal review confirm the model.";
        }
        if (firstClaimLower.contains("connector")) {
            return "Connector choice should be based on the vehicles you expect to serve and a technical site review. The MVP guidance treats connector fit as a planning question, not something Volzen should guess without checking the target driver mix.";
        }

        return "Based on the verified Volzen knowledge base: " + firstClaim;
    }

    private static String locationAnswer(String claim) {
        String cleaned = claim
                .replace(" has ", " shows ")
                .replace(" based on proxy signals; long-stay parking potential is ", ". Long-stay parking potential is ");
        return cleaned
                + " This is only a proxy signal for charging demand, not a verified count of EV drivers or guaranteed bookings.";
    }

    private static String combinedClaims(List<AdvisorKnowledgeSnippet> snippets) {
        return snippets.stream()
                .map(AdvisorKnowledgeSnippet::claim)
                .reduce("", (left, right) -> (left + " " + right).trim())
                .toLowerCase(Locale.ROOT);
    }

    private static boolean needsProfessionalReview(List<AdvisorKnowledgeSnippet> snippets) {
        return snippets.stream().anyMatch(snippet -> {
            String value = (snippet.claimType() + " " + snippet.claim()).toLowerCase();
            return value.contains("legal")
                    || value.contains("law")
                    || value.contains("electrical")
                    || value.contains("safety")
                    || value.contains("profit")
                    || value.contains("demand")
                    || value.contains("readiness")
                    || value.contains("compliance");
        });
    }

    private static AdvisorRetrievedSourceResponse toRetrievedSource(AdvisorKnowledgeSnippet snippet) {
        return new AdvisorRetrievedSourceResponse(
                snippet.sourceId(),
                snippet.claim(),
                snippet.source().title(),
                snippet.source().url(),
                snippet.source().sourceType(),
                snippet.dataAsOf());
    }
}
