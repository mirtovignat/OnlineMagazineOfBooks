package com.example.demo.exception.purchased;

import com.example.demo.exception.other.AbstractException;
import com.example.demo.exception.other.ErrorCode;

public class BalanceLimitExceededException extends AbstractException {
    private static final double MAX_BALANCE = 100_000.0;

    public BalanceLimitExceededException() {
        super(ErrorCode.BALANCE_LIMIT_EXCEED,
                String.format("Баланс кошелька не должен превышать %.2f ₽!", MAX_BALANCE));
    }

    public BalanceLimitExceededException(double currentBalance, double addAmount) {
        super(ErrorCode.BALANCE_LIMIT_EXCEED,
                String.format("Баланс не может превышать %.2f ₽. Сейчас: %.2f, пополнение: %.2f",
                        MAX_BALANCE, currentBalance, addAmount));
    }
}