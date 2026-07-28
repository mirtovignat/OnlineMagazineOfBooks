package com.example.demo.repository;

import com.example.demo.model.CartItem;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends AbstractLinkedCollectionRepository<CartItem> {
}
