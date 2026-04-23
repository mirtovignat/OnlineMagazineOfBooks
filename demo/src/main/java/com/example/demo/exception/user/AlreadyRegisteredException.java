package com.example.demo.exception.user;

import com.example.demo.exception.other.AbstractException;
import com.example.demo.exception.other.ErrorCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlreadyRegisteredException extends AbstractException {
    public AlreadyRegisteredException() {
        super(ErrorCode.ALREADY_REGISTERED, "Данные заняты!");

    }
}