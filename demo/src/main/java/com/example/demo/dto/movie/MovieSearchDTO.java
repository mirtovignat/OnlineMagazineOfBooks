package com.example.demo.dto.movie;

import com.example.demo.dto.base.Identifiable;
import com.example.demo.dto.base.Titled;

public record MovieSearchDTO(
        Long id,
        String title,
        Integer releaseYear,
        String genre,
        String director
) implements Identifiable, Titled {
}