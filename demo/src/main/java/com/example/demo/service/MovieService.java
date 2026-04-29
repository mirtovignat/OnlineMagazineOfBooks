package com.example.demo.service;

import com.example.demo.dto.movie.MovieCardDetailsForUserViewDTO;
import com.example.demo.dto.movie.MovieCardDetailsViewDTO;
import com.example.demo.dto.movie.MovieCardForUserViewDTO;
import com.example.demo.dto.movie.MovieCardViewDTO;
import com.example.demo.mapper.MovieMapper;
import com.example.demo.model.Movie;
import com.example.demo.repository.MovieRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;
    private final PurchasedService purchasedService;
    private final CartService cartService;
    private final FavouritesService favouritesService;

    public Page<MovieCardViewDTO> getMovieCards(Pageable pageable) {
        return movieRepository.findAllWithDirectorPurchasesAndReviews(pageable)
                .map(movieMapper::toCard);
    }

    public MovieCardDetailsViewDTO getCard(String title) {
        Movie movie = movieRepository.findFullByTitleOrThrow(title);
        return movieMapper.toDetails(movie);
    }

    public Page<MovieCardForUserViewDTO> getMovieCardsForUser(String username, Pageable pageable) {
        return movieRepository.findAllWithDirectorPurchasesAndReviews(pageable)
                .map(movie -> {
                    boolean bought = false, inCart = false, inFavourites = false;
                    if (username != null && !username.isBlank()) {
                        bought = purchasedService.isMoviePurchasedByBuyer(movie.getTitle(), username);
                        inCart = cartService.isMovieInCart(movie.getTitle(), username);
                        inFavourites = favouritesService.isMovieInFavourites(movie.getTitle(), username);
                    }
                    return movieMapper.toCardForUserWithStatus(movie, bought, inCart, inFavourites);
                });
    }

    public MovieCardDetailsForUserViewDTO getCardForUser(String username, String title) {
        Movie movie = movieRepository.findFullByTitleOrThrow(title);
        boolean bought = false, inCart = false, inFavourites = false;
        if (username != null && !username.isBlank()) {
            bought = purchasedService.isMoviePurchasedByBuyer(movie.getTitle(), username);
            inCart = cartService.isMovieInCart(movie.getTitle(), username);
            inFavourites = favouritesService.isMovieInFavourites(movie.getTitle(), username);
        }
        return movieMapper.toDetailsForUserWithStatus(movie, bought, inCart, inFavourites);
    }

    public Movie findByTitleOrThrow(String title) {
        return movieRepository.findFullByTitleOrThrow(title);
    }
}