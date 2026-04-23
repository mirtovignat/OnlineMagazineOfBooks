package com.example.demo.dto.authorize;

import com.example.demo.exception.user.PasswordsMismatchException;
import jakarta.validation.constraints.*;

import java.util.Objects;

public record RegisterFormDTO(
        @NotBlank(message = "Введите username")
        @Size(min = 6, max = 20) String username,

        @NotBlank(message = "Введите email")
        @Email(message = "Некорректный email адрес")
        String email,

        @NotBlank(message = "Введите пароль")
        @Size(min = 8, message = "Пароль минимум 8 символов")
        String rawPassword,

        @NotBlank(message = "Повторите пароль")
        @Size(min = 8, message = "Пароль минимум 8 символов")
        String repeatRawPassword,

        @Pattern(regexp = "^(|\\+\\d{1,3}[\\s\\-]?\\(?\\d{1,3}\\)?[\\s\\-]?\\d{3}[\\s\\-]?\\d{2}[\\s\\-]?\\d{2})$",
                message = "Некорректный номер телефона (используйте международный формат)")
        String phone,

        @NotBlank(message = "Введите фамилию")
        @Size(min = 2, max = 30, message = "Фамилия должна быть от 2 до 30 символов")
        @Pattern(regexp = "^[А-ЯЁ][а-яё]+(?:-[А-ЯЁ][а-яё]+)?$")
        String surname,

        @NotBlank(message = "Введите имя")
        @Size(min = 2, max = 30, message = "Имя должно быть от 2 до 30 символов")
        @Pattern(regexp = "^[А-ЯЁ][а-яё]+(-[А-ЯЁ][а-яё]+)?$")
        String name,

        @NotBlank(message = "Введите отчество")
        @Size(min = 2, max = 30, message = "Отчество должно быть от 2 до 30 символов")
        @Pattern(regexp = "^[А-ЯЁ][а-яё]{3,29}(вич|вна|ич|на)$")
        String patronymic,

        @NotNull
        @Pattern(regexp = "[A-Z]{3}", message = "Неверный формат валюты")
        String currencyCode
) {
    public void ifMismatch() {
        if (!Objects.equals(rawPassword(), repeatRawPassword())) {
            throw new PasswordsMismatchException();
        }
    }


}
