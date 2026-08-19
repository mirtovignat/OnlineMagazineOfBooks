package com.example.demo.dto.catalog;

import com.example.demo.dto.base.Ownable;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record ReviewViewDTO(
        String username,
        LocalDateTime addedAt,
        BigDecimal ratingValue,
        String reviewText,
        boolean own
) implements Ownable {
}