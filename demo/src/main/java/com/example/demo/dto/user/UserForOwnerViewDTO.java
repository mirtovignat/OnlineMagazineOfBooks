package com.example.demo.dto.user;

import java.math.BigDecimal;

public record UserForOwnerViewDTO(
        Long id,
        BigDecimal balance,
        String currencyCode,
        String email,
        String username,
        String phone
) {
}