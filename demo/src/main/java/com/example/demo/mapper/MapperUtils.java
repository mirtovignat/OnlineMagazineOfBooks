package com.example.demo.mapper;

import com.example.demo.config.SecurityConfig;
import lombok.AllArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;

@Component
@AllArgsConstructor
public class MapperUtils {

    private final SecurityConfig securityConfig;

    @Named("trimToNull")
    public String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    @Named("toFullName")
    public String toFullName(String surname,
                             String name,
                             String patronymic) {
        if (surname == null || name == null) {
            return null;
        }
        if (patronymic != null && !patronymic.trim().isEmpty()) {
            return surname.trim() + " " + name.trim() + " " + patronymic.trim();
        }
        return surname.trim() + " " + name.trim();
    }

    @Named("rawToEncoded")
    public String encodePassword(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        return securityConfig.passwordEncoder().encode(rawPassword);
    }

    @Named("sizeToLong")
    public Long sizeToLong(Collection<?> collection) {
        return collection == null ? 0L : (long) collection.size();
    }

    @Named("yearFromLocalDate")
    public Integer yearFromLocalDate(LocalDate localDate) {
        return localDate == null ? null : localDate.getYear();
    }
}