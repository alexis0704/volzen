package com.app.venus.modules.advisor.interfaces.dto.response;

import java.time.LocalDate;

public record AdvisorRetrievedSourceResponse(
        String sourceId,
        String claim,
        String sourceTitle,
        String sourceUrl,
        String sourceType,
        LocalDate dataAsOf) {
}
