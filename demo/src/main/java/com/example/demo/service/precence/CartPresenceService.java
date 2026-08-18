package com.example.demo.service.precence;

import com.example.demo.model.CartItem;
import com.example.demo.repository.CartItemRepository;
import org.springframework.stereotype.Service;

@Service
public class CartPresenceService extends PresenceService<CartItem> {
    public CartPresenceService(CartItemRepository cartItemRepository) {
        super(cartItemRepository);
    }
}