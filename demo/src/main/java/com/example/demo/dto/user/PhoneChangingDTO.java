package com.example.demo.dto.user;

import jakarta.validation.constraints.Pattern;

public record PhoneChangingDTO(@Pattern(regexp =
        "\\+\\d{1,3}[\\s\\-]?\\(?\\d{1,3}\\)?[\\s\\-]?\\d{3}[\\s\\-]?\\d{2}[\\s\\-]?\\d{2}",
        message = "Некорректный номер телефона (используйте международный формат)")
                               String phone) {
}
