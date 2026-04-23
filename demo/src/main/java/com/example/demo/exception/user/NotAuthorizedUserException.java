package com.example.demo.exception.user;

import com.example.demo.exception.other.AbstractException;
import com.example.demo.exception.other.ErrorCode;

public class NotAuthorizedUserException extends AbstractException {
    public NotAuthorizedUserException() {

        super(ErrorCode.USER_NOT_FOUND, "Пожалуйста, авторизуйтесь");

    }
}
