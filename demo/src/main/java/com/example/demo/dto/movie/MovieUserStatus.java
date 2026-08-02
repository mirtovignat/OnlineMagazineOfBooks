package com.example.demo.dto.movie;

public record MovieUserStatus(
        boolean bought,
        boolean inCart,
        boolean inFavourites
) {

}