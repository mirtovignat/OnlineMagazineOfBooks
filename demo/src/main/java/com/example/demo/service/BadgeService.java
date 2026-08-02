package com.example.demo.service;

import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.FavouriteMovieRepository;
import com.example.demo.repository.PurchasedMovieRepository;
import com.example.demo.repository.RatedMovieRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class BadgeService {

    private final CartItemRepository cartItemRepository;
    private final FavouriteMovieRepository favouriteMovieRepository;
    private final PurchasedMovieRepository purchasedMovieRepository;
    private final RatedMovieRepository ratedMovieRepository;

    public Map<String, Integer> getBadgeCounts(String username) {
        if (username == null) {
            return getDefaultBadges();
        }

        return Map.of(
                "cartCount", cartItemRepository.countByUsername(username),
                "favouritesCount", favouriteMovieRepository.countByUsername(username),
                "purchasesCount", purchasedMovieRepository.countByUserUsername(username),
                "ratingsCount", (int) ratedMovieRepository.countByUserUsername(username)
        );
    }

    public Map<String, Integer> getDefaultBadges() {
        return Map.of(
                "cartCount", 0,
                "favouritesCount", 0,
                "purchasesCount", 0,
                "ratingsCount", 0
        );
    }
}