package com.example.demo.dto.user;

import java.math.BigDecimal;

public record UserForOwnerViewDTO(
        Long id,
        String username,
        String email,
        String phone,
        BigDecimal balance,
        String currencyCode
) {
}