package com.example.demo.service;

import com.example.demo.dto.joined_to_user.RatedMovieForOwnerFormDTO;
import com.example.demo.dto.joined_to_user.RatedMovieForOwnerViewDTO;
import com.example.demo.dto.joined_to_user.ReviewForUserViewDTO;
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

    public boolean isRatedByUser(String title, String username) {
        return ratedMovieRepository
                .existsByUserUsernameAndMovieTitle(
                        username,
                        title
                );
    }

    public Page<RatedMovieForOwnerViewDTO> getRatedHistory(Pageable pageable, String username) {
        return ratedMovieRepository.findAllByUsername(pageable, username)
                .map(ratedMapper::toOwnerView);
    }

    public List<ReviewForUserViewDTO> getMovieReviewsForUser(String title) {
        return ratedMovieRepository.findAllByMovieTitle(title)
                .stream().map(ratedMapper::toReviewForUserView).collect(Collectors.toList());
    }

    @Transactional
    public RatedMovieForOwnerViewDTO rate(RatedMovieForOwnerFormDTO dto, String username) {
        Movie movie = movieRepository.findFullByTitleOrThrow(dto.title());
        User user = userRepository.findByUsernameOrThrow(username);
        RatedMovie ratedMovie = ratedMovieRepository
                .findByUserUsernameAndMovieTitle(username, movie.getTitle())
                .orElseGet(() -> {
                    RatedMovie rm = new RatedMovie();
                    rm.setMovie(movie);
                    rm.setUser(user);
                    return rm;
                });
        boolean isNew = ratedMovie.getId() == null;
        ratedMovie.setRatingValue(dto.rating());
        ratedMovie.setReview(dto.review());
        ratedMovie = ratedMovieRepository.save(ratedMovie);
        updateMovieRating(movie);
        if (isNew) {
            movie.setRatingsCount(movie.getRatingsCount() + 1);
            movieRepository.save(movie);
        }
        return ratedMapper.toOwnerView(ratedMovie);
    }

    @Transactional
    public RatedMovieForOwnerViewDTO updateRating(RatedMovieForOwnerFormDTO ratedMovieForOwnerFormDTO,
                                                  String username) {
        RatedMovie ratedMovie = ratedMovieRepository.findByUserUsernameAndMovieTitleOrThrow(
                username, ratedMovieForOwnerFormDTO.title());
        ratedMovie.setRatingValue(ratedMovieForOwnerFormDTO.rating());
        ratedMovie.setReview(ratedMovieForOwnerFormDTO.review());
        ratedMovie = ratedMovieRepository.save(ratedMovie);
        updateMovieRating(ratedMovie.getMovie());
        return ratedMapper.toOwnerView(ratedMovie);
    }

    @Transactional
    private void updateMovieRating(Movie movie) {
        BigDecimal avg = ratedMovieRepository.calculateAverageRating(movie);
        if (avg != null) movie.setRating(avg.setScale(1, RoundingMode.HALF_UP));
        else movie.setRating(null);
        movieRepository.save(movie);
    }

    @Transactional
    public void removeRating(String title, String username) {
        ratedMovieRepository.deleteByTitleAndUsername(title, username);
        Movie movie = movieRepository.findFullByTitleOrThrow(title);
        updateMovieRating(movie);
        movie.setRatingsCount(Math.max(0, movie.getRatingsCount() - 1));
        movieRepository.save(movie);
    }

}