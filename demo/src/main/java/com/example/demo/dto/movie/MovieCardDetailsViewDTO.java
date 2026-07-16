package com.example.demo.dto.movie;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovieCardDetailsViewDTO(
        Long id,
        String title,
        BigDecimal price,
        String genre,
        String posterUrl,
        BigDecimal rating,
        LocalDate releaseDate,
        String description,
        Long ratingsCount,
        String director,
        String formattedDuration
) implements MovieData {
}