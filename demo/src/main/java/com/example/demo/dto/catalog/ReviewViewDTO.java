package com.example.demo.dto.catalog;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReviewViewDTO(
        String username,
        LocalDateTime addedAt,
        BigDecimal ratingValue,
        String reviewText,
        boolean own
) implements OwnableDTO {
}