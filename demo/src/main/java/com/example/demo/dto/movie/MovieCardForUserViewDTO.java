package com.example.demo.dto.movie;

import java.math.BigDecimal;

public record MovieCardForUserViewDTO(
        String title,

        BigDecimal price,

        Integer releaseYear,

        String formattedDuration,

        String genre,

        BigDecimal rating,

        String director,

        boolean bought,

        boolean inCart,

        boolean inFavourites
) {

    public MovieCardForUserViewDTO{
        if (bought && inCart) {
            throw new
                    IllegalArgumentException(
                    "Фильм не может быть в корзине, " +
                            "если он куплен");
        }
    }
}