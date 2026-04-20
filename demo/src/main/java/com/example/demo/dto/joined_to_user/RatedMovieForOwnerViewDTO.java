package com.example.demo.dto.joined_to_user;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RatedMovieForOwnerViewDTO(
        String title,

        BigDecimal price,

        LocalDate releaseDate,

        String formattedDuration,

        String genre,

        BigDecimal rating,

        String description,

        String director,

        Long ratingsCount,

        Long reviewsCount,

        LocalDateTime ratedAt,

        BigDecimal ratingValue,

        String reviewText
) {
}