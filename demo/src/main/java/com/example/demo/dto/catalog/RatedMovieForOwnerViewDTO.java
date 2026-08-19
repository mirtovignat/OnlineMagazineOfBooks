package com.example.demo.dto.catalog;

import com.example.demo.dto.base.Identifiable;
import com.example.demo.dto.base.Ratable;
import com.example.demo.dto.base.Titled;

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
) implements Identifiable, Titled, Ratable {
}