package com.example.demo.dto.movie;

import com.example.demo.dto.base.Identifiable;
import com.example.demo.dto.base.Purchasable;
import com.example.demo.dto.base.Ratable;
import com.example.demo.dto.base.Titled;

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
) implements Identifiable, Titled, Ratable, Purchasable {
}