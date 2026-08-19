package com.example.demo.dto.user;

import com.example.demo.validation.ValidEmailDomain;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ProfileSettingsDTO(
        @NotBlank(message = "Никнейм не может быть пустым")
        @Size(min = 6, max = 20, message = "Никнейм должен содержать от 6 до 20 символов")
        String username,

        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Некорректный email адрес")
        @ValidEmailDomain(message = "Почтовый домен не существует")
        String email,

        @Pattern(regexp = "^$|\\+\\d{1,3}[\\s\\-]?\\(?\\d{1,3}\\)?[\\s\\-]?\\d{3}[\\s\\-]?\\d{2}[\\s\\-]?\\d{2}",
                message = "Некорректный номер телефона (используйте международный формат)")
        String phone
) {
}