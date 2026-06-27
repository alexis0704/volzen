package com.app.venus.modules.advisor.application;

import java.time.LocalDate;

public record AdvisorKnowledgeSource(
        String sourceId,
        String title,
        String url,
        LocalDate lastReviewed,
        String sourceType) {
}
