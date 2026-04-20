package com.example.demo.dto.joined_to_user;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoricalMovieForOwnerViewDTO (
        String title,

        LocalDateTime purchasedAt,

        BigDecimal priceSnapshot
) {


}
