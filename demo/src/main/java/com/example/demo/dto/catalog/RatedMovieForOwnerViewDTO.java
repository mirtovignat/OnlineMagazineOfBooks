package com.example.demo.dto.catalog;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RatedMovieForOwnerViewDTO(
        Long id,
        String title,
        String genre,
        String posterUrl,
        BigDecimal rating,
        LocalDate releaseDate,
        String review,
        LocalDateTime addedAt,
        BigDecimal ratingValue
) implements CatalogMovieData {
}