package com.example.demo.dto.response;

import com.example.demo.dto.movie.MovieCardViewDTO;
import com.example.demo.dto.movie.MovieForUser;
import com.example.demo.filter.MovieFilter;
import lombok.Builder;
import org.springframework.data.domain.Page;

@Builder
public record MoviesPageResponse(
        Page<MovieForUser<MovieCardViewDTO>> cardsPage,
        MovieFilter filter,
        boolean isSearch
) {
    public static MoviesPageResponse of(Page<MovieForUser<MovieCardViewDTO>> cardsPage) {
        return MoviesPageResponse.builder()
                .cardsPage(cardsPage)
                .filter(null)
                .isSearch(false)
                .build();
    }

    public static MoviesPageResponse of(Page<MovieForUser<MovieCardViewDTO>> cardsPage, MovieFilter filter) {
        return MoviesPageResponse.builder()
                .cardsPage(cardsPage)
                .filter(filter)
                .isSearch(true)
                .build();
    }
}