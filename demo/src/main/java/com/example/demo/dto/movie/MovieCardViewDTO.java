package com.example.demo.dto.movie;

import java.math.BigDecimal;

public record MovieCardViewDTO(
        String title,

        BigDecimal price,

        Integer releaseYear,

        String formattedDuration,

        String genre,

        BigDecimal rating
) {
}