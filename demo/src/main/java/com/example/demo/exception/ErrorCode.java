package com.example.demo.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND("Пользователь не найден: %s"),
    INSUFFICIENT_FUNDS("Недостаточно средств: нужно %s₽, доступно %s₽"),
    EMPTY_CART("Корзина пуста!"),
    EMPTY_FAVOURITES("Избранное пусто!"),
    ALREADY_REGISTERED("Ошибка при регистрации!"),
    ALREADY_TAKEN("Ошибка при изменении данных!"),
    PASSWORDS_MISMATCH("Пароли не совпадают!"),
    PASSWORD_INVALID("Неверный пароль!"),
    DATA_COINCIDENCE("Данные не изменены!"),
    BALANCE_LIMIT_EXCEED("Баланс не может превышать %s₽"),
    ENTITY_NOT_FOUND("Сущность не найдена!"),
    NOT_AUTHORIZED("Пожалуйста, авторизуйтесь!"),
    VALIDATION_ERROR("Ошибка валидации!"),
    ERROR("Произошла непредвиденная ошибка!");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String format(Object... args) {
        return String.format(message, args);
    }
}