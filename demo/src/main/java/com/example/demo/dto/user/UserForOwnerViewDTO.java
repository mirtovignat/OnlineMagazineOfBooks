package com.example.demo.dto.user;

import java.math.BigDecimal;

public record UserForOwnerViewDTO(
        String username,

        String email,

        String phone,

        BigDecimal balance,

        String currencyCode,

        int purchasesCount,

        int ratingsCount
) {
}