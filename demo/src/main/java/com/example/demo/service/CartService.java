package com.example.demo.service;

import com.example.demo.dto.joined_to_user.CartMovieForOwnerViewDTO;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.CartItemMapper;
import com.example.demo.model.CartItem;
import com.example.demo.model.Movie;
import com.example.demo.model.User;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.PurchasedMovieRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;


@Service
public class CartService extends AbstractLinkedCollectionService<CartItem, CartMovieForOwnerViewDTO> {

    private final CartItemMapper cartItemMapper;
    private final PresenceService presenceService;

    public CartService(CartItemRepository cartItemRepository,
                       UserRepository userRepository,
                       MovieRepository movieRepository,
                       PurchasedMovieRepository purchasedMovieRepository,
                       CartItemMapper cartItemMapper,
                       PresenceService presenceService) {
        super(cartItemRepository, userRepository, movieRepository, purchasedMovieRepository);
        this.cartItemMapper = cartItemMapper;
        this.presenceService = presenceService;
    }

    @Override
    protected boolean existsInCollection(Long movieId, String username) {
        return linkedCollectionRepository
                .findByMovieIdAndUserUsernameWithLock(movieId, username).isPresent();
    }

    @Override
    protected CartItem createEntity(User user, Movie movie) {
        CartItem cartItem = new CartItem();
        cartItem.setUnitPriceSnapshot(movie.getPrice());
        return cartItem;
    }

    @Override
    protected ErrorCode getEmptyErrorCode() {
        return ErrorCode.EMPTY_CART;
    }

    @Override
    protected CartMovieForOwnerViewDTO mapToDto(CartItem cartItem) {
        return cartItemMapper.toOwnerView(cartItem, presenceService);
    }

    @Override
    protected boolean shouldSkipIfPurchased() {
        return true;
    }
}