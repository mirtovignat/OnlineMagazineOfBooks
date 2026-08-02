package com.example.demo.service;

import com.example.demo.dto.joined_to_user.CartMovieForOwnerViewDTO;
import com.example.demo.dto.joined_to_user.HistoricalMovieForOwnerViewDTO;
import com.example.demo.dto.joined_to_user.LibrarianMovieForOwnerViewDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.PurchasedMapper;
import com.example.demo.model.CartItem;
import com.example.demo.model.Movie;
import com.example.demo.model.PurchasedMovie;
import com.example.demo.model.User;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.PurchasedMovieRepository;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class PurchasedService {

    @Autowired
    private final PurchasedMovieRepository purchasedMovieRepository;
    @Autowired
    private final MovieRepository movieRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final AbstractLinkedCollectionService<CartItem, CartMovieForOwnerViewDTO> cartService;
    @Autowired
    private final PurchasedMapper purchasedMapper;
    @Autowired
    private final CartItemRepository cartItemRepository;

    @Transactional(readOnly = true)
    public void validatePurchase(Long movieId, String username) {
        User user = userRepository.findByUsernameOrThrow(username);
        Movie movie = movieRepository.findByIdOrThrow(movieId);
        if (isMoviePurchasedByBuyer(movie.getId(), username)) {
            return;
        }
        ensureHasEnoughFunds(user, movie.getPrice());
    }

    @Transactional(readOnly = true)
    public void validateBulkPurchase(String username) {
        User user = userRepository.findByUsernameOrThrow(username);
        List<CartItem> cartItems = cartItemRepository.findAllByUsername(username);
        List<Movie> unpurchasedMovies = getUnpurchasedMoviesFromCart(username, cartItems);
        if (unpurchasedMovies.isEmpty()) {
            return;
        }
        ensureHasEnoughFunds(user, calculateTotalPrice(unpurchasedMovies));
    }

    @Transactional
    public void purchase(Long movieId, String username) {
        User user = userRepository.findByUsernameWithLock(username);
        Movie movie = movieRepository.findByIdOrThrow(movieId);
        if (isMoviePurchasedByBuyer(movie.getId(), username)) {
            cartService.remove(movieId, username);
            return;
        }
        user.spendMoney(movie.getPrice());
        userRepository.save(user);
        purchasedMovieRepository.save(createPurchasedMovie(user, movie));
        cartService.remove(movieId, username);
    }

    @Transactional
    public void purchase(String username) {
        User user = userRepository.findByUsernameWithLock(username);
        List<CartItem> cartItems = cartItemRepository.findAllByUsername(username);
        List<Movie> unpurchasedMovies = getUnpurchasedMoviesFromCart(username, cartItems);
        if (unpurchasedMovies.isEmpty()) {
            cartItemRepository.deleteAllByUsername(username);
            return;
        }
        user.spendMoney(calculateTotalPrice(unpurchasedMovies));
        userRepository.save(user);
        List<PurchasedMovie> purchases = unpurchasedMovies.stream()
                .map(movie -> createPurchasedMovie(user, movie))
                .toList();
        purchasedMovieRepository.saveAll(purchases);
        cartItemRepository.deleteAllByUsername(username);
    }

    @Transactional(readOnly = true)
    public Page<HistoricalMovieForOwnerViewDTO> getHistory(Pageable pageable, String username) {
        return purchasedMovieRepository.findAllByUsername(pageable, username)
                .map(purchasedMapper::toOwnerViewFromHistorical);
    }

    @Transactional(readOnly = true)
    public Page<LibrarianMovieForOwnerViewDTO> getLibrary(Pageable pageable, String username) {
        return purchasedMovieRepository.findAllByUsername(pageable, username)
                .map(purchasedMapper::toOwnerViewFromLibrarian);
    }

    @Transactional(readOnly = true)
    public boolean isMoviePurchasedByBuyer(Long movieId, String username) {
        if (movieId == null || username == null || username.isBlank()) {
            return false;
        }
        return purchasedMovieRepository.existsByMovieIdAndUserUsername(movieId, username);
    }

    @Transactional(readOnly = true)
    public Set<Long> getPurchasedMovieIds(String username) {
        return new HashSet<>(purchasedMovieRepository.findMovieIdsByUsername(username));
    }

    private List<Movie> getUnpurchasedMoviesFromCart(String username, List<CartItem> cartItems) {
        if (cartItems.isEmpty()) {
            return List.of();
        }
        List<Long> movieIds = cartItems.stream()
                .map(ci -> ci.getMovie().getId())
                .toList();
        List<Movie> movies = movieRepository.findAllById(movieIds);
        Set<Long> alreadyPurchasedIds = getPurchasedMovieIds(username);
        return movies.stream()
                .filter(movie -> !alreadyPurchasedIds.contains(movie.getId()))
                .toList();
    }

    private BigDecimal calculateTotalPrice(List<Movie> movies) {
        return movies.stream()
                .map(Movie::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void ensureHasEnoughFunds(User user, BigDecimal requiredAmount) {
        if (user.getBalance().compareTo(requiredAmount) < 0) {
            throw BusinessException.of(ErrorCode.INSUFFICIENT_FUNDS, requiredAmount, user.getBalance());
        }
    }

    private PurchasedMovie createPurchasedMovie(User user, Movie movie) {
        PurchasedMovie purchasedMovie = new PurchasedMovie();
        purchasedMovie.setMovie(movie);
        purchasedMovie.setUser(user);
        purchasedMovie.setPriceSnapshot(movie.getPrice());
        return purchasedMovie;
    }
}