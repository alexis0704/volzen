package com.app.venus.modules.advisor.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.app.venus.modules.advisor.domain.AdvisorProvider;
import com.app.venus.modules.advisor.interfaces.dto.response.AdvisorChatResponse;
import com.app.venus.modules.advisor.interfaces.dto.response.AdvisorRetrievedSourceResponse;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class AdvisorProviderOutputValidator {
    private static final Pattern SOURCE_ID = Pattern.compile("\"sourceIds\"\\s*:\\s*\\[(.*?)]", Pattern.DOTALL);
    private static final Pattern STRING_VALUE = Pattern.compile("\"([^\"]+)\"");
    private static final Pattern URL = Pattern.compile("https?://[^\\s)\\]\"]+");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdvisorChatResponse validate(
            String output,
            List<AdvisorKnowledgeSnippet> retrievedSnippets,
            AdvisorProvider provider) {
        JsonNode json = parse(output);
        String answer = json.at("/answer").asText(null);
        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("Advisor provider output is missing answer.");
        }

        boolean grounded = json.at("/grounded").asBoolean(false);
        List<String> sourceIds = sourceIds(output);
        if (grounded && sourceIds.isEmpty()) {
            throw new IllegalArgumentException("Grounded advisor output must include sourceIds.");
        }

        Set<String> allowedSourceIds = new HashSet<>();
        for (AdvisorKnowledgeSnippet snippet : retrievedSnippets) {
            allowedSourceIds.add(snippet.sourceId());
        }
        if (!allowedSourceIds.containsAll(sourceIds)) {
            throw new IllegalArgumentException("Advisor provider output invented sourceIds.");
        }
        rejectInventedUrls(answer, retrievedSnippets);

        LocalDate dataAsOf = dataAsOf(json, retrievedSnippets);
        return new AdvisorChatResponse(
                answer.trim(),
                sourceIds,
                retrievedSnippets.stream().map(this::toRetrievedSource).toList(),
                grounded,
                json.at("/needsProfessionalReview").asBoolean(false),
                dataAsOf,
                provider,
                json.at("/unsupportedReason").asText(null));
    }

    private void rejectInventedUrls(String answer, List<AdvisorKnowledgeSnippet> retrievedSnippets) {
        Set<String> allowedUrls = new HashSet<>();
        for (AdvisorKnowledgeSnippet snippet : retrievedSnippets) {
            String url = snippet.source().url();
            if (url != null && url.startsWith("http")) {
                allowedUrls.add(url);
            }
        }

        Matcher matcher = URL.matcher(answer);
        while (matcher.find()) {
            String url = matcher.group();
            if (!allowedUrls.contains(url)) {
                throw new IllegalArgumentException("Advisor provider output invented URLs.");
            }
        }
    }

    private JsonNode parse(String output) {
        try {
            return objectMapper.readTree(output);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Advisor provider output is not valid JSON.", ex);
        }
    }

    private LocalDate dataAsOf(JsonNode json, List<AdvisorKnowledgeSnippet> snippets) {
        String value = json.at("/dataAsOf").asText(null);
        if (value != null && !value.isBlank()) {
            return LocalDate.parse(value);
        }
        return snippets.stream()
                .map(AdvisorKnowledgeSnippet::dataAsOf)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());
    }

    private List<String> sourceIds(String output) {
        Matcher array = SOURCE_ID.matcher(output);
        if (!array.find()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        Matcher string = STRING_VALUE.matcher(array.group(1));
        while (string.find()) {
            values.add(string.group(1));
        }
        return values;
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
