package com.example.demo.dto.joined_to_user;


import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CartMovieForOwnerViewDTO(
        String title,

        BigDecimal unitPriceSnapshot,

        LocalDateTime addedAt,

        boolean inCart,

        boolean inFavourites
) {
}