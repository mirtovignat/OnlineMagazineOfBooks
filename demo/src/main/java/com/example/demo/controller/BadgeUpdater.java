package com.example.demo.controller;

import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.service.CartService;
import com.example.demo.service.FavouritesService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@AllArgsConstructor
@Component
public class BadgeUpdater {
    @Autowired
    private final CartService cartService;

    @Autowired
    private final FavouritesService favouritesService;

    public void updateBadges(
            UserForOwnerViewDTO userForOwnerViewDTO,
            RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("cartCount",
                cartService.getCartCount(
                        userForOwnerViewDTO.username()));
        redirectAttributes.addAttribute("favouritesCount",
                favouritesService.getFavouritesCount(
                        userForOwnerViewDTO.username()));
    }

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
