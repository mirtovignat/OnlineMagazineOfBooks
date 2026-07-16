package com.example.demo.dto.joined_to_user;

import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RatedMovieForOwnerViewDTO(
        Long id,
        String title,
        BigDecimal price,
        String genre,
        BigDecimal rating,
        LocalDateTime ratedAt,
        @Digits(integer = 2, fraction = 1, message = "Оценка должна быть от 0.0 до 10.0 с шагом 0.1")
        BigDecimal ratingValue,
        String reviewText,
        String posterUrl,
        LocalDate releaseDate,
        String director,
        String formattedDuration
) {
}