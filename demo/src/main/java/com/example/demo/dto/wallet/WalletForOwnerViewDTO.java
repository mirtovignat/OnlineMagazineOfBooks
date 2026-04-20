package com.example.demo.dto.wallet;

import java.math.BigDecimal;

public record WalletForOwnerViewDTO(
        String fullName,

        BigDecimal balance,

        String currencyCode
) {
}