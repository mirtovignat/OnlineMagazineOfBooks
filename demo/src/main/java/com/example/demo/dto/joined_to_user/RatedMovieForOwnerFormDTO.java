package com.example.demo.dto.joined_to_user;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RatedMovieForOwnerFormDTO(
        @NotBlank String title,

        @DecimalMin("0.0") @DecimalMax("10.0") @Digits(integer = 2, fraction = 1)
        BigDecimal rating,
        @Size(max = 2000) String review
) {
}