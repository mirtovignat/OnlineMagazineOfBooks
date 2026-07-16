package com.example.demo.dto.movie;

public record MovieUserStatus(
        boolean bought,
        boolean inCart,
        boolean inFavourites
) {
    public MovieUserStatus {
        if (bought && inCart) {
            throw new IllegalArgumentException
                    ("Фильм не может быть в корзине, если он куплен");
        }
    }
}