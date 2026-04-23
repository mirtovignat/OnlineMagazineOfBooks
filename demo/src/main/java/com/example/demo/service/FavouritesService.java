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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FavouritesService {
    private final FavouriteMovieRepository favouriteMovieRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final FavouritesMapper favouritesMapper;
    private final PresenceService cartService;

    public boolean isMovieInFavourites(String title, String username) {
        if (title == null || username == null || username.isBlank()) return false;
        return favouriteMovieRepository.existsByMovieTitleAndUserUsername(title, username);
    }

    public List<FavouriteMovieForOwnerViewDTO> getAllInFavouritesOfUser(String username) {
        return favouriteMovieRepository.findAllByUsername(username).stream()
                .map(favouriteMovie -> favouritesMapper.toOwnerView(favouriteMovie, cartService))
                .collect(Collectors.toList());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addToFavourites(String title, String username) {
        Movie movie = movieRepository.findFullByTitleOrThrow(title);
        User user = userRepository.findByUsernameOrThrow(username);
        FavouriteMovie favouriteMovie = new FavouriteMovie();
        favouriteMovie.setMovie(movie);
        favouriteMovie.setUser(user);
        favouriteMovieRepository.save(favouriteMovie);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeFromFavourites(String title, String username) {
        favouriteMovieRepository.deleteByMovieTitleAndUserUsername(title, username);
    }

    @Transactional
    public void removeAllFromFavourites(String username) {
        List<FavouriteMovie> favourites = favouriteMovieRepository.findAllByUsername(username);
        if (favourites.isEmpty()) return;
        for (FavouriteMovie fav : favourites) {
            Movie movie = fav.getMovie();
            if (movie != null) {
                movie.removeFavourite(fav);
                movieRepository.save(movie);
            }
        }
        favouriteMovieRepository.deleteAll(favourites);
    }

    @Transactional(readOnly = true)
    public int getFavouritesCount(String username) {
        return favouriteMovieRepository.countByUsername(username);
    }
}