package com.example.demo.service;

import com.example.demo.dto.joined_to_user.HistoricalMovieForOwnerViewDTO;
import com.example.demo.dto.joined_to_user.LibrarianMovieForOwnerViewDTO;
import com.example.demo.exception.purchased.InsufficientFundsException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PurchasedService {

    private final PurchasedMovieRepository purchasedMovieRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final PurchasedMapper purchasedMapper;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public void purchase(String title, String username) {
        User user = userRepository.findByUsernameOrThrow(username);
        Movie movie = movieRepository.findFullByTitleOrThrow(title);
        BigDecimal price = movie.getPrice();
        if (user.getBalance().compareTo(price) < 0) {
            throw new InsufficientFundsException(price, user.getBalance());
        }
        if (user.spendMoney(price)) {
            PurchasedMovie purchasedMovie = new PurchasedMovie();
            purchasedMovie.setMovie(movie);
            purchasedMovie.setUser(user);
            purchasedMovie.setPriceSnapshot(price);
            purchasedMovieRepository.save(purchasedMovie);
        }
        cartService.removeFromCart(title, username);
    }

    @Transactional
    public void purchase(String username) {
        User user = userRepository.findByUsernameWithLock(username);
        List<CartItem> cartItems = cartItemRepository.findAllByUsernameWithLock(username);
        if (cartItems.isEmpty()) return;
        List<String> titles = cartItems.stream().map(cartItem -> cartItem.getMovie().getTitle()).collect(Collectors.toList());
        List<Movie> movies = movieRepository.findAllByTitles(titles);
        Set<Long> purchasedIds = purchasedMovieRepository.findAllByUserUsername(user.getUsername())
                .stream().map(purchasedMovie -> purchasedMovie.getMovie().getId()).collect(Collectors.toSet());
        List<Movie> newMovies = movies.stream().filter(m -> !purchasedIds.contains(m.getId())).toList();
        if (newMovies.isEmpty()) {
            cartItemRepository.deleteAllByUsername(username);
            return;
        }
        BigDecimal totalPrice = newMovies.stream().map(Movie::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (user.getBalance().compareTo(totalPrice) < 0) {
            throw new InsufficientFundsException(totalPrice, user.getBalance());
        }
        user.spendMoney(totalPrice);
        List<PurchasedMovie> purchasedMovies = new ArrayList<>();
        for (Movie movie : newMovies) {
            PurchasedMovie purchasedMovie = new PurchasedMovie();
            purchasedMovie.setMovie(movie);
            purchasedMovie.setUser(user);
            purchasedMovie.setPriceSnapshot(movie.getPrice());
            purchasedMovies.add(purchasedMovie);
        }
        purchasedMovieRepository.saveAll(purchasedMovies);
        cartItemRepository.deleteAllByUsername(username);
    }

    public Page<HistoricalMovieForOwnerViewDTO> getHistory(Pageable pageable, String username) {
        return purchasedMovieRepository.findAllByUsername(pageable, username)
                .map(purchasedMapper::toOwnerViewFromHistorical);
    }

    public Page<LibrarianMovieForOwnerViewDTO> getLibrary(Pageable pageable, String username) {
        return purchasedMovieRepository.findAllByUsername(pageable, username)
                .map(purchasedMapper::toOwnerViewFromLibrarian);
    }

    public boolean isMoviePurchasedByBuyer(String title, String username) {
        if (title == null || username == null || username.isBlank()) return false;
        return purchasedMovieRepository.existsByMovieTitleAndUserUsername(title, username);
    }
}