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

    @Named("toFullName")
    public String toFullName(String surname,
                             String name,
                             String patronymic) {
        if (surname == null) {
            return null;
        }
        if (name == null) {
            return null;
        }
        if (patronymic == null) {
            return null;
        }
        return surname + " " + name + " " + patronymic;
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

    @Named("nullableNumberToLong")
    public Long nullableNumberToLong(Number number) {
        return number == null ? 0L : number.longValue();
    }

    @Named("yearFromLocalDate")
    public Integer yearFromLocalDate(LocalDate localDate) {
        return localDate == null ? null : localDate.getYear();
    }
}
