package com.example.demo.exception.authorize;

import com.example.demo.exception.other.AbstractException;
import com.example.demo.exception.other.ErrorCode;

public class InvalidPasswordException extends AbstractException {
    public InvalidPasswordException() {
        super(ErrorCode.PASSWORD_INVALID, "Неверный пароль!");
    }
}
