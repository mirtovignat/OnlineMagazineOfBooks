package com.example.demo.dto.catalog;

import com.example.demo.dto.base.Identifiable;
import com.example.demo.dto.base.Purchasable;
import com.example.demo.dto.base.Ratable;
import com.example.demo.dto.base.Titled;

import java.math.BigDecimal;

public record CartMovieForOwnerViewDTO(
        Long id,
        String title,
        boolean inCart,
        boolean inFavourites,
        String genre,
        String posterUrl,
        BigDecimal rating,
        Integer releaseYear,
        String director,
        String formattedDuration,
        BigDecimal price
) implements Identifiable, Titled, Ratable, Purchasable {
}