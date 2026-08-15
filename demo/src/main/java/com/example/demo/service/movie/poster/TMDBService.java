package com.example.demo.service.movie.poster;

import com.example.demo.config.TMDBConfig;
import com.example.demo.dto.movie.TMDBMovieDTO;
import com.example.demo.dto.response.TMDBMovieSearchResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
public class TMDBService {

    private final TMDBConfig tmdbConfig;
    private final RestClient restClient;

    public TMDBService(TMDBConfig tmdbConfig) {
        this.tmdbConfig = tmdbConfig;
        this.restClient = RestClient.builder()
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    public TMDBMovieDTO findMovie(String title, Integer year) {
        System.out.println(
                "🔍 Ищу в TMDB: " +
                        title +
                        " (год: " +
                        year +
                        ")"
        );
        String url = UriComponentsBuilder
                .fromUriString(tmdbConfig.getApiUrl() + "/search/movie")
                .queryParam("query", title)
                .queryParam("language", "ru-RU")
                .queryParam("include_adult", false)
                .queryParamIfPresent(
                        "year",
                        Optional.ofNullable(year)
                )
                .toUriString();
        TMDBMovieSearchResponse tmdbMovieSearchResponse = restClient.get()
                .uri(url)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + tmdbConfig.getAccessToken()
                )
                .retrieve()
                .body(TMDBMovieSearchResponse.class);
        if (tmdbMovieSearchResponse == null
                || tmdbMovieSearchResponse.results() == null
                || tmdbMovieSearchResponse.results().isEmpty()) {

            System.out.println(
                    "❌ Нет результатов для: " + title
            );

            return null;
        }
        System.out.println(
                "📦 Найдено результатов: " +
                        tmdbMovieSearchResponse.results().size()
        );
        for (TMDBMovieDTO tmdbMovieDTO : tmdbMovieSearchResponse.results()) {
            if (tmdbMovieDTO.title() != null
                    && tmdbMovieDTO.title().equalsIgnoreCase(title)
                    && hasPoster(tmdbMovieDTO)) {
                System.out.println(
                        "✅ Найдено по точному названию: " +
                                tmdbMovieDTO.title()
                );
                return tmdbMovieDTO;
            }
        }
        if (year != null) {
            for (TMDBMovieDTO tmdbMovieDTO : tmdbMovieSearchResponse.results()) {
                if (tmdbMovieDTO.releaseDate() != null
                        && tmdbMovieDTO.releaseDate().startsWith(year.toString())
                        && hasPoster(tmdbMovieDTO)) {
                    System.out.println(
                            "✅ Найдено по году: " +
                                    tmdbMovieDTO.title() +
                                    " (" +
                                    tmdbMovieDTO.releaseDate() +
                                    ")"
                    );
                    return tmdbMovieDTO;
                }
            }
        }
        for (TMDBMovieDTO movie : tmdbMovieSearchResponse.results()) {
            if (hasPoster(movie)) {
                System.out.println(
                        "✅ Найден первый результат с постером: " +
                                movie.title()
                );
                return movie;
            }
        }
        System.out.println(
                "❌ Ни у одного результата нет постера: " +
                        title
        );
        return null;
    }

    private boolean hasPoster(TMDBMovieDTO movie) {
        return movie.posterPath() != null
                && !movie.posterPath().isBlank();
    }

    public String getPosterUrl(String posterPath) {

        if (posterPath == null || posterPath.isBlank()) {
            return null;
        }

        return tmdbConfig.getImageUrl()
                + "/w500"
                + posterPath;
    }
}

