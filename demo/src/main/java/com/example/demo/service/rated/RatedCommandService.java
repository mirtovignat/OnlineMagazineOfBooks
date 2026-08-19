package com.example.demo.service.rated;

import com.example.demo.dto.catalog.RatedMovieForOwnerFormDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.model.base.AbstractCatalogItem;
import com.example.demo.model.entity.Movie;
import com.example.demo.model.entity.RatedMovie;
import com.example.demo.model.entity.User;
import com.example.demo.repository.entity.RatedMovieRepository;
import com.example.demo.service.movie.MovieService;
import com.example.demo.service.user.UserQueryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@AllArgsConstructor
@Service
public class RatedCommandService {

    private final MovieService movieService;
    private final UserQueryService userQueryService;
    private final RatedMovieRepository ratedMovieRepository;
    private final RatedQueryService ratedQueryService;

    @Transactional
    public void addOrUpdateRating(Long movieId, Long userId,
                                  RatedMovieForOwnerFormDTO ratedMovieForOwnerFormDTO) {
        Movie movie = movieService.getMovie(movieId);
        User user = userQueryService.getUser(userId);
        RatedMovie ratedMovie = ratedMovieRepository
                .findByMovieIdAndUserIdWithLock(movieId, userId)
                .orElseGet(() -> AbstractCatalogItem.init(
                        RatedMovie.builder().build(),
                        user,
                        movie
                ));
        if (ratedQueryService.isUnchanged(ratedMovieForOwnerFormDTO, ratedMovie)) {
            throw BusinessException.of(ErrorCode.DATA_COINCIDENCE);
        }
        ratedMovie.setRatingValue(ratedMovieForOwnerFormDTO.rating());
        ratedMovie.setReview(ratedMovieForOwnerFormDTO.review());
        ratedMovieRepository.saveAndFlush(ratedMovie);
        synchronizeMovieRatingInternal(movie, movieId);
    }

    @Transactional
    public void deleteRating(Long movieId, Long userId) {
        Movie movie = movieService.getMovie(movieId);
        int deleted = ratedMovieRepository.deleteByMovieIdAndUserId(movieId, userId);
        if (deleted == 0) {
            throw BusinessException.of(ErrorCode.ENTITY_NOT_FOUND);
        }
        synchronizeMovieRatingInternal(movie, movieId);
    }

    private void synchronizeMovieRatingInternal(Movie movie, Long movieId) {
        BigDecimal average = ratedMovieRepository.calculateAverageRating(movie);
        long actualCount = ratedMovieRepository.countByMovieId(movieId);
        movie.setRating(average != null ? average.setScale(1, RoundingMode.HALF_UP) : null);
        movie.setRatingsCount(Math.toIntExact(actualCount));
        movieService.save(movie);
    }
}