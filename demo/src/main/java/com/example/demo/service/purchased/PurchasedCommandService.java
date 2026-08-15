package com.example.demo.service.purchased;

import com.example.demo.dto.joined_to_user.CartMovieForOwnerViewDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.model.CartItem;
import com.example.demo.model.Movie;
import com.example.demo.model.PurchasedMovie;
import com.example.demo.model.User;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.PurchasedMovieRepository;
import com.example.demo.service.linked_collection.AbstractLinkedCollectionService;
import com.example.demo.service.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Service
public class PurchasedCommandService {

    private final PurchasedMovieRepository purchasedMovieRepository;
    protected final AbstractLinkedCollectionService<CartItem,
            CartMovieForOwnerViewDTO> cartService;
    private final MovieRepository movieRepository;
    private final UserService userService;
    private final PurchasedQueryService purchasedQueryService;

    @Transactional
    public void purchase(Long movieId, String username) {
        purchaseMovies(List.of(movieId), username, true);
    }

    @Transactional
    public void purchaseBulk(String username) {
        List<CartItem> cartItems = cartService.findAll(username);
        if (cartItems.isEmpty()) {
            return;
        }
        List<Long> movieIds = cartItems.stream()
                .map(item -> item.getMovie().getId())
                .toList();
        purchaseMovies(movieIds, username, false);
    }

    private void purchaseMovies(List<Long> requestedMovieIds, String username, boolean isSinglePurchase) {
        User user = userService.getUser(username);
        Set<Long> alreadyPurchasedIds = purchasedQueryService.getPurchasedMovieIds(username);
        List<Long> idsToBuy = requestedMovieIds.stream()
                .filter(id -> !alreadyPurchasedIds.contains(id))
                .distinct()
                .toList();
        if (idsToBuy.isEmpty()) {
            clearCartFor(requestedMovieIds, username, isSinglePurchase);
            return;
        }
        List<Movie> moviesToBuy = movieRepository.findAllById(idsToBuy);
        BigDecimal totalPrice = moviesToBuy.stream()
                .map(Movie::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (user.getBalance().compareTo(totalPrice) < 0) {
            throw BusinessException.of(ErrorCode.INSUFFICIENT_FUNDS, totalPrice, user.getBalance());
        }
        user.spendMoney(totalPrice);
        userService.createUser(user);
        List<PurchasedMovie> purchases = moviesToBuy.stream()
                .map(movie -> {
                    PurchasedMovie purchased = new PurchasedMovie();
                    purchased.setMovie(movie);
                    purchased.setUser(user);
                    purchased.setPriceSnapshot(movie.getPrice());
                    return purchased;
                })
                .toList();
        purchasedMovieRepository.saveAll(purchases);
        clearCartFor(requestedMovieIds, username, isSinglePurchase);
    }

    private void clearCartFor(List<Long> requestedMovieIds,
                              String username,
                              Boolean isSinglePurchase) {
        if (isSinglePurchase && !requestedMovieIds.isEmpty()) {
            cartService.remove(requestedMovieIds.get(0), username);
        } else {
            cartService.deleteAll(username);
        }
    }
}