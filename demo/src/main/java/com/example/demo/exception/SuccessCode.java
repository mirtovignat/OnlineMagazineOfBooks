package com.example.demo.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessCode {
    REVIEW_HAS_BEEN_UPDATED("Отзыв обновлен"),
    REVIEW_HAS_BEEN_SAVED("Отзыв добавлен"),
    REVIEW_HAS_BEEN_DELETED("Отзыв удален"),
    ADDED("Добавлено"),
    REMOVED("Удалено"),
    PROFILE_HAS_BEEN_CHANGED("Профиль изменен"),
    PASSWORD_HAS_BEEN_CHANGED("Пароль изменен"),
    PHONE_HAS_BEEN_REMOVED("Телефон удален"),
    BALANCE_HAS_BEEN_TOPPED_UP("Баланс пополнен"),
    PURCHASED("Куплено"),
    CLEARED("Очищено"),
    ACCOUNT_HAS_BEEN_REMOVED("Аккаунт успешно удален");

    private final String message;
}