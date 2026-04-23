package com.example.demo.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailChangingDTO(
        @NotBlank
        @Email(message = "Некорректный email адрес") String email
) {
}
