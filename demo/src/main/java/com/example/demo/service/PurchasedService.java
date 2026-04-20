package com.example.demo.service;

import com.example.demo.dto.joined_to_user.CartMovieForOwnerViewDTO;
import com.example.demo.dto.joined_to_user.HistoricalMovieForOwnerViewDTO;
import com.example.demo.dto.joined_to_user.LibrarianMovieForOwnerViewDTO;
import com.example.demo.exception.purchased.InsufficientFundsException;
import com.example.demo.mapper.PurchasedMapper;
import com.example.demo.model.Movie;
import com.example.demo.model.PurchasedMovie;
import com.example.demo.model.User;
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
        User user = userRepository.findByUsernameOrThrow(username);
        List<String> titles = cartService.getAllInCartOfUser(username)
                .stream().map(CartMovieForOwnerViewDTO::title).collect(Collectors.toList());
        if (titles.isEmpty()) return;
        List<Movie> movies = movieRepository.findAllByTitles(titles);
        Set<Long> purchasedIds = purchasedMovieRepository.findAllByUserUsername(user.getUsername())
                .stream().map(pm -> pm.getMovie().getId()).collect(Collectors.toSet());
        List<Movie> newMovies = movies.stream().filter(m -> !purchasedIds.contains(m.getId())).toList();
        if (newMovies.isEmpty()) {
            cartService.removeAllFromCart(username);
            return;
        }
        BigDecimal totalPrice = newMovies.stream().map(Movie::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (user.getBalance().compareTo(totalPrice) < 0) {
            throw new InsufficientFundsException(totalPrice, user.getBalance());
        }
        if (user.spendMoney(totalPrice)) {
            List<PurchasedMovie> purchasedMovies = new ArrayList<>();
            for (Movie movie : newMovies) {
                PurchasedMovie pm = new PurchasedMovie();
                pm.setMovie(movie);
                pm.setUser(user);
                pm.setPriceSnapshot(movie.getPrice());
                purchasedMovies.add(pm);
            }
            purchasedMovieRepository.saveAll(purchasedMovies);
        }
        cartService.removeAllFromCart(username);
    }

    @Transactional
    public void removeFromPurchased(String title, String username) {
        purchasedMovieRepository.deleteByTitleAndUsername(title, username);
    }



    public PurchasedMovie findPurchasedMovieOrThrow(String username, String title) {
        return purchasedMovieRepository.findFullByUsernameAndTitleOrThrow(username, title);
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