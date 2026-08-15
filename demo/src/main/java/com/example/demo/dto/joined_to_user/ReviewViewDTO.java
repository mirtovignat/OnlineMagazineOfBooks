package com.example.demo.dto.joined_to_user;

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