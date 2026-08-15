package com.example.demo.service.purchased;

import com.example.demo.repository.PurchasedMovieRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;

@AllArgsConstructor
@Service
public class PurchasedQueryService {

    private final PurchasedMovieRepository purchasedMovieRepository;

    @Transactional(readOnly = true)
    public boolean isMoviePurchasedByUser(Long movieId, String username) {
        if (movieId == null || username == null || username.isBlank()) {
            return false;
        }
        return purchasedMovieRepository.existsByMovieIdAndUserUsername(
                movieId, username);
    }

    @Transactional(readOnly = true)
    public Set<Long> getPurchasedMovieIds(String username) {
        return new LinkedHashSet<>(purchasedMovieRepository
                .findMovieIdsByUsername(username));
    }
}