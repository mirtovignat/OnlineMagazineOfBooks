package com.example.demo.dto.catalog;

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

) implements LinkedCollectionMovieData {
}