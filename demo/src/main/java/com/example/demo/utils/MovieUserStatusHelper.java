package com.example.demo.utils;

import com.example.demo.dto.joined_to_user.CartMovieForOwnerViewDTO;
import com.example.demo.dto.joined_to_user.FavouriteMovieForOwnerViewDTO;
import com.example.demo.dto.movie.MovieCardViewDTO;
import com.example.demo.dto.movie.MovieForUser;
import com.example.demo.dto.movie.MovieUserStatus;
import com.example.demo.model.CartItem;
import com.example.demo.model.FavouriteMovie;
import com.example.demo.model.Movie;
import com.example.demo.service.AbstractLinkedCollectionService;
import com.example.demo.service.PurchasedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class MovieUserStatusHelper {

    private final PurchasedService purchasedService;
    private final AbstractLinkedCollectionService<FavouriteMovie, FavouriteMovieForOwnerViewDTO> favouritesService;
    private final AbstractLinkedCollectionService<CartItem, CartMovieForOwnerViewDTO> cartService;


    private record UserStatuses(Set<Long> bought, Set<Long> cart, Set<Long> favourites) {}

    private UserStatuses getUserStatuses(String username) {
        return new UserStatuses(
                safeSet(purchasedService.getPurchasedMovieIds(username)),
                safeSet(cartService.getMovieIds(username)),
                safeSet(favouritesService.getMovieIds(username))
        );
    }

    public Page<MovieForUser<MovieCardViewDTO>> enrichWithUserStatuses(
            Page<Movie> moviePage,
            String username,
            Function<Movie, MovieCardViewDTO> toCardMapper) {

        UserStatuses statuses = getUserStatuses(username);
        return moviePage.map(movie -> {
            boolean isBought = statuses.bought.contains(movie.getId());
            boolean isInCart = !isBought && statuses.cart.contains(movie.getId());
            boolean isInFav = statuses.favourites.contains(movie.getId());
            return new MovieForUser<>(
                    toCardMapper.apply(movie),
                    new MovieUserStatus(isBought, isInCart, isInFav)
            );
        });
    }

    private Set<Long> safeSet(Set<Long> set) {
        return set == null ? Set.of() : set;
    }
}