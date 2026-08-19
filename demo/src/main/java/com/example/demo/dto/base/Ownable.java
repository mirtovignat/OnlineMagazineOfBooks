package com.example.demo.dto.base;

import java.time.LocalDateTime;

public interface Ownable {
    boolean own();
    LocalDateTime addedAt();
}