package com.example.demo.dto.catalog;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoricalMovieForOwnerViewDTO(
        Long id,
        String title,
        BigDecimal priceSnapshot,
        LocalDateTime addedAt
) implements CatalogMovieData {
}