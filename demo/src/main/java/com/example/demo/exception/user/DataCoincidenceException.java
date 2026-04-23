package com.example.demo.exception.user;

import com.example.demo.exception.other.AbstractException;
import com.example.demo.exception.other.ErrorCode;

public class DataCoincidenceException extends AbstractException {
    public DataCoincidenceException() {
        super(ErrorCode.DATA_COINCIDENCE, "Измените данные!");
    }
}
