package com.example.demo.dto.response;

import com.example.demo.dto.joined_to_user.ReviewViewDTO;

import java.math.BigDecimal;
import java.util.List;

public record ReviewsPageResponse(
        List<ReviewViewDTO> reviews,
        BigDecimal avgRating,
        long totalReviews,
        boolean hasOwnReview,
        String movieTitle,
        Long movieId
) {

}