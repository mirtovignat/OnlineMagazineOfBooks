package com.example.demo.service;

import com.example.demo.dto.joined_to_user.CartMovieForOwnerViewDTO;
import com.example.demo.exception.cart.EmptyException;
import com.example.demo.mapper.CartItemMapper;
import com.example.demo.model.CartItem;
import com.example.demo.model.Movie;
import com.example.demo.model.User;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final CartItemMapper cartItemMapper;
    private final PresenceService favouritesService;

    @Transactional(readOnly = true)
    public int getCartCount(String username) {
        return cartItemRepository.countByUsername(username);
    }

    @Transactional(readOnly = true)
    public List<CartMovieForOwnerViewDTO> getAllInCartOfUser(String username) {
        return cartItemRepository.findAllByUsername(username)
                .stream()
                .map(cartItem -> cartItemMapper
                        .toOwnerView(cartItem, favouritesService))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isMovieInCart(String title, String username) {
        if (title == null || username == null || username.isBlank()) return false;
        return cartItemRepository.existsByMovieTitleAndUserUsername(title, username);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addToCart(String title, String username) {
        User user = userRepository.findByUsernameOrThrow(username);
        Movie movie = movieRepository.findFullByTitleOrThrow(title);
        CartItem cartItem = new CartItem();
        cartItem.setMovie(movie);
        cartItem.setUser(user);
        cartItem.setUnitPriceSnapshot(movie.getPrice());
        cartItemRepository.save(cartItem);

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeFromCart(String title, String username) {
        cartItemRepository.deleteByTitleAndUsername(title, username);
    }

    @Transactional
    public void removeAllFromCart(String username) {
        int count = cartItemRepository.countByUsername(username);
        if (count == 0) {
            throw new EmptyException();
        }
        cartItemRepository.deleteAllByUsername(username);
    }
}