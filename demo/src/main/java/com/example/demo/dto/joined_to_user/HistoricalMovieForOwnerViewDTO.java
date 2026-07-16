package com.example.demo.dto.joined_to_user;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record HistoricalMovieForOwnerViewDTO(
        Long id,

        String title,

        LocalDateTime purchasedAt,

        BigDecimal priceSnapshot,

        String genre,

        String posterUrl,

        BigDecimal rating,

        LocalDate releaseDate,

        Long ratingsCount,

        String director,

        String formattedDuration
) {


}
