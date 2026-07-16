package com.example.demo.utils;

import org.springframework.stereotype.Component;

@Component
public class SqlSearchEscaper {

    public String escapeLike(String rawValue) {
        if (rawValue == null) {
            return "";
        }
        return rawValue.trim().replace("%", "\\%").replace("_", "\\_");
    }

    public String prepareForSearch(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        return escapeLike(rawValue);
    }
}