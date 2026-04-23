package com.example.demo.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsernameChangingDTO(
        @NotBlank
        @Size(min = 6, max = 20) String username
) {
}
