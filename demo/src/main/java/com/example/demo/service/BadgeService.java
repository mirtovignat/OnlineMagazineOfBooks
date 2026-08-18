package com.example.demo.service;

import com.example.demo.dto.badges.BadgeCountsDTO;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.FavouriteMovieRepository;
import com.example.demo.repository.PurchasedMovieRepository;
import com.example.demo.repository.RatedMovieRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class BadgeService {

    private final CartItemRepository cartItemRepository;
    private final FavouriteMovieRepository favouriteMovieRepository;
    private final PurchasedMovieRepository purchasedMovieRepository;
    private final RatedMovieRepository ratedMovieRepository;

    @Transactional(readOnly = true)
    public BadgeCountsDTO getBadgeCounts(Long id) {
        if (id == null) {
            return BadgeCountsDTO.empty();
        }
        return new BadgeCountsDTO(
                cartItemRepository.countByUserId(id),
                favouriteMovieRepository.countByUserId(id),
                purchasedMovieRepository.countByUserId(id),
                ratedMovieRepository.countByUserId(id)
        );
    }
}