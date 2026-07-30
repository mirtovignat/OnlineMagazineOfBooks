package com.example.demo.exception; // Укажи свой пакет

import lombok.Getter;

@Getter
public enum SuccessCode {
    REVIEW_HAS_BEEN_UPDATED_SUCCESSFULLY("Отзыв обновлен"),
    REVIEW_HAS_BEEN_SAVED_SUCCESSFULLY("Отзыв добавлен"),
    REVIEW_HAS_BEEN_DELETED_SUCCESSFULLY("Отзыв удален"),
    ADDED_SUCCESSFULLY("Добавлено"),
    REMOVED_SUCCESSFULLY("Удалено"),
    PROFILE_HAS_BEEN_CHANGED_SUCCESSFULLY("Профиль изменен"),
    PASSWORD_HAS_BEEN_CHANGED_SUCCESSFULLY("Пароль изменен"),
    PHONE_HAS_BEEN_REMOVED_SUCCESSFULLY("Телефон удален"),
    BALANCE_HAS_BEEN_TOPPED_UP_SUCCESSFULLY("Баланс пополнен"),
    PURCHASED_SUCCESSFULLY("Куплено"),
    CLEARED_SUCCESSFULLY("Очищено");

    private final String message;

    SuccessCode(String message) {
        this.message = message;
    }

    public String format(Object... args) {
        return String.format(message, args);
    }
}