package com.example.demo.dto.catalog;

import com.example.demo.dto.base.Identifiable;
import com.example.demo.dto.base.Titled;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoricalMovieForOwnerViewDTO(
        Long id,
        String title,
        BigDecimal priceSnapshot,
        LocalDateTime addedAt
) implements Identifiable, Titled {
}