package com.example.demo.utils;

import com.example.demo.dto.movie.MovieSearchDTO;
import com.example.demo.filter.MovieFilter;
import com.example.demo.model.Movie;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.specification.MovieSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MovieSearchHelper {
    @Autowired
    private final MovieRepository movieRepository;
    @Autowired
    private final SqlSearchEscaper sqlSearchEscaper;

    public List<Movie> findMoviesByDtoList(List<MovieSearchDTO> movieSearchDTOS) {
        if (movieSearchDTOS == null || movieSearchDTOS.isEmpty()) {
            return Collections.emptyList();
        }
        return movieSearchDTOS.stream()
                .map(movieSearchDTO -> movieRepository.findByTitleAndDirectorAndGenreAndReleaseYear(
                        movieSearchDTO.title(),
                        movieSearchDTO.director(),
                        movieSearchDTO.genre(),
                        movieSearchDTO.releaseYear()
                ))
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0))
                .distinct()
                .collect(Collectors.toList());
    }

    public Page<Movie> findMoviesByMovieFilter(MovieFilter movieFilter, Pageable pageable) {
        if (movieFilter == null) {
            return Page.empty(pageable);
        }
        Specification<Movie> spec = MovieSpecifications.byFilter(movieFilter)
                .and(MovieSpecifications.fetchPurchasesAndReviews());

        return movieRepository.findAll(spec, pageable);
    }

    public List<MovieSearchDTO> getSuggestions(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        Pageable limit = PageRequest.of(0, 10);
        String escaped = sqlSearchEscaper.escapeLike(query.trim());
        return movieRepository.findByTitleContainingIgnoreCase(escaped, limit)
                .stream()
                .map(movie -> new MovieSearchDTO(
                        movie.getId(),
                        movie.getTitle(),
                        movie.getReleaseDate() != null ? movie.getReleaseDate().getYear() : null,
                        movie.getGenre(),
                        movie.getDirector()
                ))
                .collect(Collectors.toList());
    }
}