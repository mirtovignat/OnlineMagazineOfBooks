package com.example.demo.exception.cart;

import com.example.demo.exception.other.AbstractException;
import com.example.demo.exception.other.ErrorCode;

public class EmptyException extends AbstractException {

    public EmptyException() {
        super(ErrorCode.EMPTY_CART,
                "Корзина пуста!");
    }
}
