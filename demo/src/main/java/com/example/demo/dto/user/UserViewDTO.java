package com.example.demo.dto.user;

public record UserViewDTO(
        String username,

        Long purchasesCount,

        Long ratingsCount
) {
}