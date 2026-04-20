package com.example.demo.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Duration;

@Converter(autoApply = true)
public class DurationConverter implements AttributeConverter<Duration, Long> {
    @Override
    public Long convertToDatabaseColumn(Duration duration) {
        System.out.println("Converting Duration to Long: " + duration);
        return duration == null ? null : duration.getSeconds();
    }

    @Override
    public Duration convertToEntityAttribute(Long durationInDatabase) {
        System.out.println("Converting Long to Duration: " + durationInDatabase);
        return durationInDatabase == null ? null : Duration.ofSeconds(durationInDatabase);
    }
}
