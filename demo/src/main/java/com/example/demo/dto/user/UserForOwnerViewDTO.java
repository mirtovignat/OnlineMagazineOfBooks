package com.example.demo.dto.user;

import lombok.With;

import java.math.BigDecimal;

@With
public record UserForOwnerViewDTO(
        Long id,
        String username,
        int cartCount,
        int favouritesCount,
        String email,
        String phone,
        BigDecimal balance,
        String currencyCode,
        int purchasesCount,
        int ratingsCount
) {
}