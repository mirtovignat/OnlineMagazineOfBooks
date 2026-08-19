package com.example.demo.dto.catalog;

import com.example.demo.dto.base.Identifiable;
import com.example.demo.dto.base.Ratable;
import com.example.demo.dto.base.Titled;

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
) implements Identifiable, Titled, Ratable {
}