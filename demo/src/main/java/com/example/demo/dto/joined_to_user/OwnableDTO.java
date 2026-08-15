package com.example.demo.dto.joined_to_user;

import java.time.LocalDateTime;

public interface OwnableDTO {
    boolean own();

    LocalDateTime addedAt();
}
