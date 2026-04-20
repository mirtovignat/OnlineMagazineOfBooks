package com.example.demo.service;

import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.FavouriteMovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PresenceService {
    private final CartItemRepository cartItemRepository;
    private final FavouriteMovieRepository favouriteMovieRepository;

    public boolean isInCart(String title, String username) {
        return cartItemRepository
                .existsByMovieTitleAndUserUsername(title, username);
    }

    public boolean isInFavourites(String title, String username) {
        return favouriteMovieRepository
                .existsByMovieTitleAndUserUsername(title, username);
    }
}