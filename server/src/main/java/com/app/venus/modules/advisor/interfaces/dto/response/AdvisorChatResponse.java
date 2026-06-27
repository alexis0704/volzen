package com.app.venus.modules.advisor.interfaces.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.app.venus.modules.advisor.domain.AdvisorProvider;

public record AdvisorChatResponse(
        String answer,
        List<String> sourceIds,
        List<AdvisorRetrievedSourceResponse> retrievedSources,
        boolean grounded,
        boolean needsProfessionalReview,
        LocalDate dataAsOf,
        AdvisorProvider provider,
        String unsupportedReason) {
}
