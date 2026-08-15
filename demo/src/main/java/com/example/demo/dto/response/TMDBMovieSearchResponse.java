package com.example.demo.dto.response;

import com.example.demo.dto.movie.TMDBMovieDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TMDBMovieSearchResponse(
        int page,
        List<TMDBMovieDTO> results,
        @JsonProperty("total_pages")
        int totalPages,
        @JsonProperty("total_results")
        int totalResults
) {
}