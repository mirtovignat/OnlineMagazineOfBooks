package com.example.demo.service.linked_collection;

import com.example.demo.dto.badges.BadgeCountsDTO;
import com.example.demo.dto.catalog.CartMovieForOwnerViewDTO;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.CartItemMapper;
import com.example.demo.model.AbstractCatalogItem;
import com.example.demo.model.CartItem;
import com.example.demo.model.Movie;
import com.example.demo.model.User;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.PurchasedMovieRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.precence.CartPresenceService;
import org.springframework.stereotype.Service;

@Service
public class CartService extends AbstractLinkedCollectionService<CartItem,
        CartMovieForOwnerViewDTO> {

    private final CartItemMapper cartItemMapper;
    private final CartPresenceService cartPresenceService;

    public CartService(CartItemRepository cartItemRepository,
                       UserRepository userRepository,
                       MovieRepository movieRepository,
                       PurchasedMovieRepository purchasedMovieRepository,
                       CartItemMapper cartItemMapper,
                       CartPresenceService cartPresenceService) {
        super(cartItemRepository, userRepository, movieRepository,
                purchasedMovieRepository);
        this.cartItemMapper = cartItemMapper;
        this.cartPresenceService = cartPresenceService;
    }

    @Override
    public BadgeCountsDTO updateBadge(BadgeCountsDTO badgeCountsDTO, Long newCount) {
        return badgeCountsDTO.toBuilder().cartCount(newCount).build();
    }

    @Override
    public boolean existsInCollection(Long movieId, Long userId) {
        return linkedCollectionRepository
                .findByMovieIdAndUserIdWithLock(movieId, userId).isPresent();
    }

    @Override
    protected CartItem createEntity(User user, Movie movie) {
        return AbstractCatalogItem.init(CartItem.builder()
                .quantity((short) 1)
                .build(), user, movie);
    }

    @Override
    protected ErrorCode getEmptyErrorCode() {
        return ErrorCode.EMPTY_CART;
    }

    @Override
    protected CartMovieForOwnerViewDTO mapToDto(CartItem cartItem) {
        return cartItemMapper.toOwnerView(cartItem, cartPresenceService);
    }

    @Override
    protected boolean shouldSkipIfPurchased() {
        return true;
    }

}