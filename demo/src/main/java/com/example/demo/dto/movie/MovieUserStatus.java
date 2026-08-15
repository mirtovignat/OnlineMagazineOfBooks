package com.example.demo.dto.movie;

import lombok.Builder;

@Builder
public record MovieUserStatus(
        boolean bought,
        boolean inCart,
        boolean inFavourites
) {
}