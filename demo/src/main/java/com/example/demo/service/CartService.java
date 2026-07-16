package com.example.demo.service;

import com.example.demo.dto.joined_to_user.CartMovieForOwnerViewDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.CartItemMapper;
import com.example.demo.model.CartItem;
import com.example.demo.model.Movie;
import com.example.demo.model.User;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.PurchasedMovieRepository;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final PurchasedMovieRepository purchasedMovieRepository;
    private final CartItemMapper cartItemMapper;
    private final PresenceService presenceService;

    @Transactional(readOnly = true)
    public int getCartCount(String username) {
        return cartItemRepository.countByUsername(username);
    }

    @Transactional(readOnly = true)
    public Set<Long> getCartMovieIds(String username) {
        return new HashSet<>(cartItemRepository.findMovieIdsByUsername(username));
    }

    @Transactional(readOnly = true)
    public List<CartMovieForOwnerViewDTO> getAllInCartOfUser(String username) {
        return cartItemRepository.findAllByUsername(username).stream()
                .map(cartItem -> cartItemMapper.toOwnerView(cartItem, presenceService))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isMovieInCart(Long movieId, String username) {
        if (movieId == null || username == null || username.isBlank()) {
            return false;
        }
        return cartItemRepository.existsByMovieIdAndUserUsername(movieId, username);
    }

    @Transactional
    public void addToCart(Long movieId, String username) {
        User user = userRepository.findByUsernameOrThrow(username);
        Movie movie = movieRepository.findFullByIdOrThrow(movieId);

        if (purchasedMovieRepository.existsByMovieIdAndUserUsername(movie.getId(), username)) {
            return;
        }

        CartItem existing = cartItemRepository
                .findByMovieIdAndUserUsernameWithLock(movie.getId(), username)
                .orElse(null);
        if (existing != null) {
            return;
        }

        CartItem cartItem = new CartItem();
        cartItem.setMovie(movie);
        cartItem.setUser(user);
        cartItem.setUnitPriceSnapshot(movie.getPrice());
        cartItemRepository.save(cartItem);
    }

    @Transactional
    public void removeFromCart(Long movieId, String username) {
        Movie movie = movieRepository.findFullByIdOrThrow(movieId);
        cartItemRepository.deleteByMovieIdAndUsername(movie.getId(), username);
    }

    @Transactional
    public void removeAllFromCart(String username) {
        if (cartItemRepository.countByUsername(username) == 0) {
            throw BusinessException.of(ErrorCode.EMPTY_CART);
        }
        cartItemRepository.deleteAllByUsername(username);
    }
}