package com.app.venus.modules.advisor.application;

import java.time.LocalDate;

public record AdvisorKnowledgeSnippet(
        String id,
        String sourceId,
        String claimType,
        String claim,
        AdvisorKnowledgeSource source,
        LocalDate dataAsOf,
        int score) {

    AdvisorKnowledgeSnippet withScore(int score) {
        return new AdvisorKnowledgeSnippet(id, sourceId, claimType, claim, source, dataAsOf, score);
    }
}
