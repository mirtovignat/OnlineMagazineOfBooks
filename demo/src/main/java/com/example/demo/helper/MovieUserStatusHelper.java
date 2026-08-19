package com.example.demo.helper;

import com.example.demo.dto.catalog.CartMovieForOwnerViewDTO;
import com.example.demo.dto.catalog.FavouriteMovieForOwnerViewDTO;
import com.example.demo.dto.movie.MovieCardViewDTO;
import com.example.demo.dto.movie.MovieForUser;
import com.example.demo.dto.movie.MovieUserStatus;
import com.example.demo.model.entity.CartItem;
import com.example.demo.model.entity.FavouriteMovie;
import com.example.demo.model.entity.Movie;
import com.example.demo.service.linked_collection.AbstractLinkedCollectionService;
import com.example.demo.service.purchased.PurchasedQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class MovieUserStatusHelper {


    private final PurchasedQueryService purchasedService;
    private final AbstractLinkedCollectionService<FavouriteMovie, FavouriteMovieForOwnerViewDTO>
            favouritesService;
    private final AbstractLinkedCollectionService<CartItem, CartMovieForOwnerViewDTO> cartService;

    private record UserStatuses(Set<Long> bought, Set<Long> cart, Set<Long> favourites) {
    }

    private UserStatuses getUserStatuses(Long userId) {
        return new UserStatuses(
                safeSet(purchasedService.getPurchasedMovieIds(userId)),
                safeSet(cartService.getMovieIds(userId)),
                safeSet(favouritesService.getMovieIds(userId))
        );
    }

    public Page<MovieForUser<MovieCardViewDTO>> enrichWithUserStatuses(
            Page<Movie> moviePage,
            Long userId,
            Function<Movie, MovieCardViewDTO> toCardMapper) {

        UserStatuses statuses = getUserStatuses(userId);
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