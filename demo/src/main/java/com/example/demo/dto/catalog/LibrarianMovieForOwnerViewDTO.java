package com.example.demo.dto.catalog;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LibrarianMovieForOwnerViewDTO(
        Long id,
        String title,
        String genre,
        String posterUrl,
        BigDecimal rating,
        LocalDate releaseDate,
        String director
) implements CatalogMovieData {
}