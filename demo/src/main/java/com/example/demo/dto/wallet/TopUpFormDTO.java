package com.example.demo.dto.wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

public record TopUpFormDTO(
        @Digits(integer = 12, fraction = 2)
        @DecimalMin(value = "0.01", inclusive = true)
        Double amount
) {
}