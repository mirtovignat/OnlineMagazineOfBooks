package com.example.demo.dto.joined_to_user;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CartMovieForOwnerViewDTO(
        Long id,

        String title,

        BigDecimal unitPriceSnapshot,

        LocalDateTime addedAt,

        boolean inCart,

        boolean inFavourites,

        String genre,

        String posterUrl,

        BigDecimal rating,

        Integer releaseYear,

        String director,

        String formattedDuration
) {
}