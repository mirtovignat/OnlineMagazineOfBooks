package com.example.demo.exception.purchased;

import com.example.demo.exception.other.AbstractException;
import com.example.demo.exception.other.ErrorCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InsufficientFundsException extends AbstractException {
    private BigDecimal requiredMoney;
    private BigDecimal balance;

    public InsufficientFundsException(BigDecimal requiredMoney,
                                      BigDecimal balance) {
        super(ErrorCode.INSUFFICIENT_FUNDS, "Вы запросили: " + requiredMoney + "₽, текущий баланс: " + balance + "₽, нехватка: " +
                (requiredMoney.doubleValue() - balance.doubleValue() + "₽")
        );
        this.requiredMoney = requiredMoney;
        this.balance = balance;
    }
}
