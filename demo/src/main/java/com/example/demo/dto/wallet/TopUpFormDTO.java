package com.example.demo.dto.wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

public record TopUpFormDTO(
        @Digits(integer = 12, fraction = 2)
        @DecimalMin("0.01")
        BigDecimal amount
) {
}