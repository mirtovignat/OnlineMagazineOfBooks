package com.example.demo.dto.movie;

import java.math.BigDecimal;

public record MovieCardViewDTO(
        Long id,
        String title,
        BigDecimal price,
        String genre,
        String posterUrl,
        BigDecimal rating,
        Integer releaseYear,
        Long ratingsCount,
        String director,
        String formattedDuration
) implements MovieData {
}