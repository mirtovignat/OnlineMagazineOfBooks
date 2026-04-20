package com.example.demo.dto.wallet;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

public record TopUpFormDTO(
        @Digits(integer = 12, fraction = 2)
        @DecimalMin("0.0")
        @DecimalMax("5000.0")
        Double amount
) {
}
