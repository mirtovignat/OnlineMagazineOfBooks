package com.example.demo.dto.authorize;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LoginFormDTO(
        @NotNull Identifier identifier,
        @NotBlank String identifierValue,
        @NotBlank @Size(min = 8) String rawPassword) {
}