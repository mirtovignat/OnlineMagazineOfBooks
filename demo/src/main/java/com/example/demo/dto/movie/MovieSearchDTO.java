package com.example.demo.dto.movie;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MovieSearchDTO(
        Long id,
        String title,
        @JsonProperty("releaseYear") Integer releaseYear,
        String genre,
        String director
) {
}