package com.example.demo.dto.joined_to_user;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReviewViewDTO(
        String title,
        String username,
        LocalDateTime ratedAt,
        BigDecimal ratingValue,
        String reviewText,
        boolean own
) {
}