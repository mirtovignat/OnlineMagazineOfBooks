package com.example.demo.service.movie.poster;

import com.example.demo.dto.movie.TMDBMovieDTO;
import com.example.demo.model.Movie;
import com.example.demo.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MoviePosterService {

    private final TMDBService tmdbService;
    private final PosterService posterService;
    private final MovieRepository movieRepository;

    @Transactional
    public String replacePosterFromTMDB(Long movieId) throws Exception {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Фильм не найден: " + movieId
                        )
                );
        Integer year = movie.getReleaseDate() != null
                ? movie.getReleaseDate().getYear()
                : null;
        TMDBMovieDTO tmdbMovie =
                tmdbService.findMovie(movie.getTitle(), year);
        if (tmdbMovie == null) {
            throw new IllegalStateException(
                    "Фильм не найден в TMDB: " + movie.getTitle()
            );
        }
        if (tmdbMovie.posterPath() == null) {
            throw new IllegalStateException(
                    "У фильма нет постера в TMDB: " + movie.getTitle()
            );
        }
        String tmdbPosterUrl =
                tmdbService.getPosterUrl(tmdbMovie.posterPath());

        String fileName =
                "movie-" + movie.getId() + ".jpg";
        String minioUrl =
                posterService.uploadPosterFromUrl(
                        tmdbPosterUrl,
                        fileName
                );
        movie.setPosterUrl(minioUrl);
        movieRepository.save(movie);
        return minioUrl;
    }
}