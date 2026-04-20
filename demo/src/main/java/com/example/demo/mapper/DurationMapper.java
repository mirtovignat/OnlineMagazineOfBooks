package com.example.demo.mapper;

import org.mapstruct.Mapper;

import java.time.Duration;

@Mapper(componentModel = "spring")
public interface DurationMapper {

    default String toString(Duration duration) {
        if (duration == null) return null;

        long seconds = duration.getSeconds();

        Long hours = seconds / 3600;
        Long minutes = (seconds % 3600) / 60;
        Long secs = seconds % 60;

        return String.format("%d:%02d:%02d", hours, minutes, secs);
    }

    default Duration toDuration(String value) {
        if (value == null || value.isBlank()) return null;

        String normalized = value.trim();

        if (!normalized.matches("^\\d+:\\d{1,2}:\\d{1,2}$")) {
            throw new IllegalArgumentException(
                    String.format("Неверный формат длительности. Ожидается ЧЧ:ММ:СС (например: 2:30:15). Получено: '%s'", value)
            );
        }

        String[] parts = normalized.split(":");

        try {
            long hours = Long.parseLong(parts[0]);
            long minutes = Long.parseLong(parts[1]);
            long seconds = getSeconds(value, parts, minutes);

            return Duration.ofSeconds(hours * 3600 + minutes * 60 + seconds);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Неверный числовой формат в длительности: '%s'", value), e
            );
        }
    }

    private Long getSeconds(String value, String[] parts, long minutes) {
        long seconds = Long.parseLong(parts[2]);

        if (minutes >= 60) {
            throw new IllegalArgumentException(
                    String.format("Минуты должны быть меньше 60. Получено: %d в '%s'", minutes, value)
            );
        }

        if (seconds >= 60) {
            throw new IllegalArgumentException(
                    String.format("Секунды должны быть меньше 60. Получено: %d в '%s'", seconds, value)
            );
        }
        return seconds;
    }

}