package com.example.demo.exception.authorize;

import com.example.demo.exception.other.AbstractException;
import com.example.demo.exception.other.ErrorCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserNotFoundException extends AbstractException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND, "Пользователь не найден!");
    }
}