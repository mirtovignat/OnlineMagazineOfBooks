package com.example.demo.dto.user;

import com.example.demo.exception.user.DataCoincidenceException;
import com.example.demo.exception.user.PasswordsMismatchException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

public record PasswordChangingDTO(
        @NotBlank(message = "Введите текущий пароль")
        @Size(min = 8, message = "Пароль минимум 8 символов")
        String currentPassword,

        @NotBlank(message = "Введите пароль")
        @Size(min = 8, message = "Пароль минимум 8 символов")
        String rawPassword,

        @NotBlank(message = "Повторите пароль")
        @Size(min = 8, message = "Пароль минимум 8 символов")
        String repeatRawPassword
) {
    public void isMismatch() {
        if (!Objects.equals(rawPassword(), repeatRawPassword())) {
            throw new PasswordsMismatchException();
        }
    }

    public void isCoincidence() {
        if (Objects.equals(currentPassword(), rawPassword())) {
            throw new DataCoincidenceException();
        }
    }
}
