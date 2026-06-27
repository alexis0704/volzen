package com.app.venus.modules.advisor.application;

import java.util.List;

public record AdvisorRetrievalResult(
        List<AdvisorKnowledgeSnippet> snippets,
        boolean supported,
        String unsupportedReason) {
}
