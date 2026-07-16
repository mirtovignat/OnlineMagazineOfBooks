package com.example.demo.service;

import com.example.demo.dto.joined_to_user.FavouriteMovieForOwnerViewDTO;
import com.example.demo.mapper.FavouritesMapper;
import com.example.demo.model.FavouriteMovie;
import com.example.demo.model.Movie;
import com.example.demo.model.User;
import com.example.demo.repository.FavouriteMovieRepository;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class FavouritesService {
    private final FavouriteMovieRepository favouriteMovieRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final FavouritesMapper favouritesMapper;
    private final PresenceService presenceService;

    @Transactional(readOnly = true)
    public boolean isMovieInFavourites(Long movieId, String username) {
        if (movieId == null || username == null || username.isBlank()) {
            return false;
        }
        Movie movie = movieRepository.findFullByIdOrThrow(movieId);
        return favouriteMovieRepository.existsByMovieIdAndUserUsername(movie.getId(), username);
    }

    @Transactional(readOnly = true)
    public Set<Long> getFavouriteMovieIds(String username) {
        return new HashSet<>(favouriteMovieRepository.findMovieIdsByUsername(username));
    }

    @Transactional(readOnly = true)
    public List<FavouriteMovieForOwnerViewDTO> getAllInFavouritesOfUser(String username) {
        return favouriteMovieRepository.findAllByUsername(username).stream()
                .map(favouriteMovie -> favouritesMapper.toOwnerView(favouriteMovie, presenceService))
                .toList();
    }

    @Transactional
    public void addToFavourites(Long movieId, String username) {
        Movie movie = movieRepository.findFullByIdOrThrow(movieId);
        User user = userRepository.findByUsernameOrThrow(username);

        FavouriteMovie existing = favouriteMovieRepository
                .findByMovieIdAndUserUsernameWithLock(movie.getId(), username)
                .orElse(null);
        if (existing != null) {
            return;
        }

        FavouriteMovie favouriteMovie = new FavouriteMovie();
        favouriteMovie.setMovie(movie);
        favouriteMovie.setUser(user);
        favouriteMovieRepository.save(favouriteMovie);
    }

    @Transactional
    public void removeFromFavourites(Long movieId, String username) {
        Movie movie = movieRepository.findFullByIdOrThrow(movieId);
        favouriteMovieRepository.deleteByMovieIdAndUserUsername(movie.getId(), username);
    }

    @Transactional
    public void removeAllFromFavourites(String username) {
        if (favouriteMovieRepository.findAllByUsername(username).isEmpty()) return;
        favouriteMovieRepository.deleteAllByUsername(username);
    }

    @Transactional(readOnly = true)
    public int getFavouritesCount(String username) {
        return favouriteMovieRepository.countByUsername(username);
    }
}