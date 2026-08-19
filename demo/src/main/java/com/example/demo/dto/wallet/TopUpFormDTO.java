package com.example.demo.dto.wallet;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TopUpFormDTO(
        @NotNull(message = "Сумма не может быть пустой")
        @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
        @DecimalMax(value = "100000.00", message = "Сумма не может превышать 100000,00 ₽")
        @Digits(integer = 12, fraction = 2, message = "Некорректный формат суммы")
        BigDecimal amount
) {
}