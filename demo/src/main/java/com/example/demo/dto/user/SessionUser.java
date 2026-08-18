package com.example.demo.dto.user;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SessionUser(
        Long id,
        BigDecimal balance,
        String currencyCode,
        String firstUsernameLetter
) {

    public static SessionUser empty() {
        return SessionUser.builder()
                .id(null)
                .balance(BigDecimal.ZERO)
                .currencyCode("₽")
                .firstUsernameLetter("u")
                .build();
    }

    public SessionUser withBalance(BigDecimal newBalance) {
        return SessionUser.builder()
                .id(this.id)
                .balance(newBalance)
                .currencyCode(this.currencyCode)
                .firstUsernameLetter(this.firstUsernameLetter)
                .build();
    }

    public SessionUser withFirstLetter(String username) {
        return SessionUser.builder()
                .id(this.id)
                .balance(this.balance)
                .currencyCode(this.currencyCode)
                .firstUsernameLetter(firstLetterFrom(username))
                .build();
    }

    public static SessionUser from(UserForOwnerViewDTO fullUser) {
        if (fullUser == null) {
            return empty();
        }
        return SessionUser.builder()
                .id(fullUser.id())
                .balance(fullUser.balance())
                .currencyCode(fullUser.currencyCode())
                .firstUsernameLetter(firstLetterFrom(fullUser.username()))
                .build();
    }

    private static String firstLetterFrom(String username) {
        if (username == null || username.isEmpty()) {
            return "u";
        }
        return username.substring(0, 1);
    }
}