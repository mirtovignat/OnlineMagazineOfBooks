package com.example.demo.dto.joined_to_user;

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