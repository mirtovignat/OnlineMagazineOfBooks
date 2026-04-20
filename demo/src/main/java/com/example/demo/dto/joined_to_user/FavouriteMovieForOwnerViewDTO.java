package com.example.demo.dto.joined_to_user;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FavouriteMovieForOwnerViewDTO(
        String title,

        BigDecimal price,

        LocalDate releaseDate,

        String formattedDuration,

        String genre,

        BigDecimal rating,

        String description,

        String buyerReviewText,

        boolean inCart,

        boolean inFavourites,

        LocalDateTime userReviewWrittenAt
) {
}