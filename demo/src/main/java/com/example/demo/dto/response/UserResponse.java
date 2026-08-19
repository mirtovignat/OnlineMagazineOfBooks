package com.example.demo.dto.response;

import com.example.demo.dto.user.UserForOwnerViewDTO;
import lombok.Builder;

@Builder
public record UserResponse(
        UserForOwnerViewDTO userForOwner,
        long purchasesCount,
        long ratingsCount
) {
}
