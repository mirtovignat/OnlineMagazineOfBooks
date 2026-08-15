package com.example.demo.dto.authorize;

import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.validation.ValidEmailDomain;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.Objects;

@Builder
public record RegisterFormDTO(
        @NotBlank(message = "Никнейм не может быть пустым")
        @Size(min = 6, max = 20, message = "Никнейм должен содержать от 6 до 20 символов")
        String username,

        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Некорректный email адрес")
        @ValidEmailDomain(message = "Почтовый домен не существует")
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

        @Size(max = 30, message = "Отчество должно быть до 30 символов")
        @Pattern(regexp = "^(|[А-ЯЁ][а-яё]{3,29}(вич|вна|ич|на))$",
                message = "Неверный формат отчества")
        String patronymic,

        @NotNull
        @Pattern(regexp = "[A-Z]{3}", message = "Неверный формат валюты")
        String currencyCode
) {

    public static RegisterFormDTO initial() {
        return builder()
                .currencyCode("RUB")
                .build();
    }

    public void ifMismatch() {
        if (!Objects.equals(rawPassword(), repeatRawPassword())) {
            throw BusinessException.of(ErrorCode.PASSWORDS_MISMATCH);
        }
    }
}