package com.example.demo.filter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MovieFilter(
        BigDecimal priceFrom,
        BigDecimal priceTo,
        LocalDate releaseDateFrom,
        LocalDate releaseDateTo,
        Integer durationFrom,
        Integer durationTo,
        List<String> genres,
        BigDecimal ratingFrom,
        BigDecimal ratingTo,
        List<String> directors
) {
}