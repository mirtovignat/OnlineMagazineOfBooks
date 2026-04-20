package com.example.demo.dto.joined_to_user;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LibrarianMovieForOwnerViewDTO(
        String title,

        BigDecimal price,

        LocalDate releaseDate,

        String formattedDuration,

        String genre,

        BigDecimal rating,

        String review,

        String description,

        Long purchasesCount,

        Long reviewCount
) {
}
