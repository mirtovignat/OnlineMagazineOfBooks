package com.example.demo.dto.response;

import com.example.demo.dto.catalog.ReviewViewDTO;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record ReviewsPageResponse(
        List<ReviewViewDTO> reviews,
        BigDecimal avgRating,
        long totalReviews,
        boolean hasOwnReview,
        String movieTitle,
        Long movieId
) {

    public static ReviewsPageResponse of(
            List<ReviewViewDTO> reviews,
            BigDecimal avgRating,
            long totalReviews,
            boolean hasOwnReview,
            String movieTitle,
            Long movieId
    ) {
        return ReviewsPageResponse.builder()
                .reviews(reviews)
                .avgRating(avgRating)
                .totalReviews(totalReviews)
                .hasOwnReview(hasOwnReview)
                .movieTitle(movieTitle != null ? movieTitle : "Фильм")
                .movieId(movieId != 0 ? movieId : 0)
                .build();
    }
}