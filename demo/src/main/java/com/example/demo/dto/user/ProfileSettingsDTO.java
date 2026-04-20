package com.example.demo.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileSettingsDTO(
        @NotBlank
        String username,

        @NotBlank
        @Email(message = "Некорректный email адрес") String email,

        @Pattern(regexp =
                "\\+\\d{1,3}[\\s\\-]?\\(?\\d{1,3}\\)?[\\s\\-]?\\d{3}[\\s\\-]?\\d{2}[\\s\\-]?\\d{2}",
                message = "Некорректный номер телефона (используйте международный формат)")
        String phone
) {
}
