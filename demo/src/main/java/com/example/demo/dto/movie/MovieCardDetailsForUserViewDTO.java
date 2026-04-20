package com.example.demo.dto.movie;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovieCardDetailsForUserViewDTO(
        String title,

        BigDecimal price,

        LocalDate releaseDate,

        Integer releaseYear,

        String formattedDuration,

        String genre,

        BigDecimal rating,

        String description,

        String director,

        Long purchasesCount,

        Long ratingsCount,

        boolean bought,

        boolean inCart,

        boolean inFavourites
) {
}
