package com.example.demo.dto.badges;

import lombok.Builder;

@Builder(toBuilder = true)
public record BadgeCountsDTO(
        Long cartCount,
        Long favouritesCount,
        Long purchasesCount,
        Long ratingsCount
) {
}