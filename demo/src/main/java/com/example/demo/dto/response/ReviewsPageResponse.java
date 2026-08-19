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
}