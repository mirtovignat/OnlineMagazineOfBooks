package com.example.demo.dto.joined_to_user;

import java.math.BigDecimal;

public record CartMovieForOwnerViewDTO(
        BigDecimal unitPriceSnapshot,

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
) {
}