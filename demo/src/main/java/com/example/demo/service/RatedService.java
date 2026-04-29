package com.example.demo.service;

import com.example.demo.dto.joined_to_user.RatedMovieForOwnerFormDTO;
import com.example.demo.dto.joined_to_user.RatedMovieForOwnerViewDTO;
import com.example.demo.dto.joined_to_user.ReviewForUserViewDTO;
import com.example.demo.exception.user.DataCoincidenceException;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RatedService {

    private final RatedMovieRepository ratedMovieRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final RatedMapper ratedMapper;

    private boolean isUnchanged(RatedMovieForOwnerFormDTO ratedMovieForOwnerFormDTO, RatedMovie ratedMovie) {
        return ratedMovieForOwnerFormDTO.rating().compareTo(ratedMovie.getRatingValue()) == 0
                && (ratedMovieForOwnerFormDTO.review() == null ? ratedMovie.getReview() == null : ratedMovieForOwnerFormDTO.review().equals(ratedMovie.getReview()));
    }

    private void throwIfNoChanges(boolean unchanged) {
        if (unchanged) throw new DataCoincidenceException();
    }

    @Transactional(readOnly = true)
    public RatedMovie findRatedMovieByUsernameAndTitle(String username, String title) {
        return ratedMovieRepository.findByUsernameAndTitleOrThrow(username, title);
    }

    @Transactional(readOnly = true)
    public boolean isRatedByUser(String title, String username) {
        return ratedMovieRepository.existsByUserUsernameAndMovieTitle(username, title);
    }

    @Transactional(readOnly = true)
    public Page<RatedMovieForOwnerViewDTO> getRatedHistory(Pageable pageable, String username) {
        return ratedMovieRepository.findAllByUsername(pageable, username)
                .map(ratedMapper::toOwnerView);
    }

    @Transactional(readOnly = true)
    public List<ReviewForUserViewDTO> getMovieReviewsForUser(String title) {
        return ratedMovieRepository.findAllByMovieTitle(title)
                .stream()
                .map(ratedMapper::toReviewForUserView)
                .collect(Collectors.toList());
    }

    @Transactional
    public void rate(RatedMovieForOwnerFormDTO ratedMovieForOwnerFormDTO, String username) {
        Movie movie = movieRepository.findFullByTitleOrThrow(ratedMovieForOwnerFormDTO.title());
        User user = userRepository.findByUsernameOrThrow(username);

        RatedMovie ratedMovie = ratedMovieRepository
                .findByUsernameAndTitle(username, movie.getTitle())
                .orElseGet(() -> {
                    RatedMovie newRatedMovie = new RatedMovie();
                    newRatedMovie.setMovie(movie);
                    newRatedMovie.setUser(user);
                    return newRatedMovie;
                });

        boolean isNew = ratedMovie.getId() == null;
        if (!isNew) {
            throwIfNoChanges(isUnchanged(ratedMovieForOwnerFormDTO, ratedMovie));
        }

        ratedMovie.setRatingValue(ratedMovieForOwnerFormDTO.rating());
        ratedMovie.setReview(ratedMovieForOwnerFormDTO.review());
        ratedMovieRepository.save(ratedMovie);

        if (isNew) {
            movie.setRatingsCount(movie.getRatingsCount() + 1);
            movieRepository.save(movie);
        }

        updateMovieRating(movie);
    }

    @Transactional
    public void updateRating(RatedMovieForOwnerFormDTO ratedMovieForOwnerFormDTO, String username) {
        RatedMovie ratedMovie = findRatedMovieByUsernameAndTitle(username, ratedMovieForOwnerFormDTO.title());
        throwIfNoChanges(isUnchanged(ratedMovieForOwnerFormDTO, ratedMovie));
        ratedMovie.setRatingValue(ratedMovieForOwnerFormDTO.rating());
        ratedMovie.setReview(ratedMovieForOwnerFormDTO.review());
        ratedMovieRepository.save(ratedMovie);
        updateMovieRating(ratedMovie.getMovie());
    }

    @Transactional
    public void removeRating(String title, String username) {
        ratedMovieRepository.deleteByTitleAndUsername(title, username);
        Movie movie = movieRepository.findFullByTitleOrThrow(title);
        updateMovieRating(movie);
        movie.setRatingsCount(Math.max(0, movie.getRatingsCount() - 1));
        movieRepository.save(movie);
    }

    private void updateMovieRating(Movie movie) {
        BigDecimal avg = ratedMovieRepository.calculateAverageRating(movie);
        if (avg != null) {
            movie.setRating(avg.setScale(1, RoundingMode.HALF_UP));
        } else {
            movie.setRating(null);
        }
        movieRepository.save(movie);
    }
}