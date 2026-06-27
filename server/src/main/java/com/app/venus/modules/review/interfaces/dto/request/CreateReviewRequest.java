package com.app.venus.modules.review.interfaces.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
        @Min(1) @Max(5) int rating,
        @NotBlank @Size(max = 1000) String comment) {
}
