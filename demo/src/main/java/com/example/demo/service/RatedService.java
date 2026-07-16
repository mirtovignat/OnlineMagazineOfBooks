package com.example.demo.service;

import com.example.demo.dto.joined_to_user.RatedMovieForOwnerFormDTO;
import com.example.demo.dto.joined_to_user.RatedMovieForOwnerViewDTO;
import com.example.demo.dto.joined_to_user.ReviewViewDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.RatedMapper;
import com.example.demo.model.Movie;
import com.example.demo.model.RatedMovie;
import com.example.demo.model.User;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.RatedMovieRepository;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@AllArgsConstructor
public class RatedService {

    private final RatedMovieRepository ratedMovieRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final RatedMapper ratedMapper;

    @Transactional(readOnly = true)
    public Page<RatedMovieForOwnerViewDTO> getRatedHistory(String username, Pageable pageable) {
        return ratedMovieRepository.findAllByUsername(pageable, username)
                .map(ratedMapper::toOwnerView);
    }

    @Transactional(readOnly = true)
    public Page<ReviewViewDTO> getReviewsByMovieId(Long movieId, Pageable pageable, String currentUsername) {
        Movie movie = movieRepository.findFullByIdOrThrow(movieId);
        return ratedMovieRepository.findAllByMovieId(movie.getId(), pageable)
                .map(ratedMovie -> ratedMapper.toReviewView(ratedMovie, currentUsername));
    }

    @Transactional(readOnly = true)
    public RatedMovieForOwnerFormDTO getPreFilledForm(Long movieId, String username) {
        return ratedMovieRepository.findByMovieIdAndUserUsername(movieId, username)
                .map(ratedMapper::toFormView)
                .orElse(new RatedMovieForOwnerFormDTO(movieId, null, null));
    }

    @Transactional
    public void addOrUpdateRating(Long movieId, String username, RatedMovieForOwnerFormDTO ratedMovieForOwnerFormDTO) {
        Movie movie = movieRepository.findFullByIdOrThrow(movieId);
        User user = userRepository.findByUsernameOrThrow(username);

        RatedMovie ratedMovie = ratedMovieRepository
                .findByMovieIdAndUserUsernameWithLock(movie.getId(), username)
                .orElseGet(() -> {
                    RatedMovie rm = new RatedMovie();
                    rm.setMovie(movie);
                    rm.setUser(user);
                    return rm;
                });
        boolean isNew = ratedMovie.getId() == null;
        if (!isNew && isUnchanged(ratedMovieForOwnerFormDTO, ratedMovie)) {
            throw BusinessException.of(ErrorCode.DATA_COINCIDENCE);
        }
        ratedMovie.setRatingValue(ratedMovieForOwnerFormDTO.rating());
        ratedMovie.setReview(ratedMovieForOwnerFormDTO.review());
        ratedMovieRepository.save(ratedMovie);
        if (isNew) {
            movieRepository.incrementRatingsCount(movie.getId());
            movieRepository.flush();
        }
        updateMovieRating(movie);
    }

    @Transactional
    public void deleteRating(Long movieId, String username) {
        Movie movie = movieRepository.findFullByIdOrThrow(movieId);
        ratedMovieRepository.deleteByMovieIdAndUsername(movie.getId(), username);
        movieRepository.decrementRatingsCount(movie.getId());
        movieRepository.flush();
        updateMovieRating(movie);
    }

    private boolean isUnchanged(RatedMovieForOwnerFormDTO ratedMovieForOwnerFormDTO,
                                RatedMovie ratedMovie) {
        boolean isRatingSame = ratedMovieForOwnerFormDTO.rating().compareTo(ratedMovie.getRatingValue()) == 0;
        String newReview = (ratedMovieForOwnerFormDTO.review() != null) ? ratedMovieForOwnerFormDTO.review().trim() : "";
        String oldReview = (ratedMovie.getReview() != null) ? ratedMovie.getReview().trim() : "";
        boolean isReviewSame = newReview.equals(oldReview);
        return isRatingSame && isReviewSame;
    }

    private void updateMovieRating(Movie movie) {
        BigDecimal average = ratedMovieRepository.calculateAverageRating(movie);
        movie.setRating(average != null ? average.setScale(1, RoundingMode.HALF_UP) : null);
        movieRepository.save(movie);
    }

    @Transactional(readOnly = true)
    public BigDecimal getMovieRating(Long movieId) {
        return movieRepository.findFullByIdOrThrow(movieId).getRating();
    }
}