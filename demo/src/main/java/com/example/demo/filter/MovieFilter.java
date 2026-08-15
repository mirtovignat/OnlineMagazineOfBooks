package com.example.demo.filter;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record MovieFilter(
        String title,

        @PositiveOrZero(message = "Цена от не может быть отрицательной")
        BigDecimal priceFrom,

        @PositiveOrZero(message = "Цена до не может быть отрицательной")
        BigDecimal priceTo,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate releaseDateFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate releaseDateTo,

        @Min(value = 1, message = "Длительность от должна быть не менее 1 минуты")
        Integer durationFrom,

        @Min(value = 1, message = "Длительность до должна быть не менее 1 минуты")
        Integer durationTo,

        @DecimalMin(value = "0.1", message = "Рейтинг от должен быть не менее 0.1")
        @DecimalMax(value = "10.0", message = "Рейтинг от должен быть не более 10.0")
        BigDecimal ratingFrom,

        @DecimalMin(value = "0.1", message = "Рейтинг до должен быть не менее 0.1")
        @DecimalMax(value = "10.0", message = "Рейтинг до должен быть не более 10.0")
        BigDecimal ratingTo,

        List<String> genres,
        List<String> directors
) {
    public MovieFilter normalize() {
        return MovieFilter.builder()
                .title(title)
                .genres(genres)
                .directors(directors)
                .priceFrom(min(priceFrom, priceTo))
                .priceTo(max(priceFrom, priceTo))
                .releaseDateFrom(min(releaseDateFrom, releaseDateTo))
                .releaseDateTo(max(releaseDateFrom, releaseDateTo))
                .durationFrom(min(durationFrom, durationTo))
                .durationTo(max(durationFrom, durationTo))
                .ratingFrom(min(ratingFrom, ratingTo))
                .ratingTo(max(ratingFrom, ratingTo))
                .build();
    }

    private static <T extends Comparable<? super T>> T min(T a, T b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.compareTo(b) <= 0 ? a : b;
    }

    private static <T extends Comparable<? super T>> T max(T a, T b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.compareTo(b) >= 0 ? a : b;
    }
}