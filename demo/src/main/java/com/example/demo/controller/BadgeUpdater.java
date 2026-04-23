package com.example.demo.controller;

import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.service.CartService;
import com.example.demo.service.FavouritesService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@AllArgsConstructor
@Component
public class BadgeUpdater {
    private final CartService cartService;
    private final FavouritesService favouritesService;

    public void updateBadges(
            UserForOwnerViewDTO userForOwnerViewDTO,
            Model model) {
        model.addAttribute("cartCount",
                cartService.getCartCount(
                        userForOwnerViewDTO.username()));
        model.addAttribute("favouritesCount",
                favouritesService.getFavouritesCount(
                        userForOwnerViewDTO.username()));
    }
}
