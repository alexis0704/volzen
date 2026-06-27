package com.app.venus.modules.review.interfaces.dto.response;

import java.time.Instant;

import com.app.venus.modules.review.domain.Review;

public record ReviewResponse(
        String id,
        String orderId,
        String providerId,
        String authorName,
        String authorAvatarUrl,
        int rating,
        String comment,
        Instant createdAt) {

    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getOrder().getId(),
                review.getProviderStation().getId(),
                review.getAuthor().getFullName(),
                review.getAuthor().getAvatarUrl(),
                review.getRating(),
                review.getComment(),
                review.getCreatedInstant());
    }
}
