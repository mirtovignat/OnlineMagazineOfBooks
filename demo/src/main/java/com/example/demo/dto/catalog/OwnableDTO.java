package com.example.demo.dto.catalog;

import java.time.LocalDateTime;

public interface OwnableDTO {
    boolean own();

    LocalDateTime addedAt();
}
