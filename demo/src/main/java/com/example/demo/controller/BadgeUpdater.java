package com.example.demo.controller;

import com.example.demo.dto.joined_to_user.CartMovieForOwnerViewDTO;
import com.example.demo.dto.joined_to_user.FavouriteMovieForOwnerViewDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.model.CartItem;
import com.example.demo.model.FavouriteMovie;
import com.example.demo.service.AbstractLinkedCollectionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@AllArgsConstructor
@Component
public class BadgeUpdater {
    private final AbstractLinkedCollectionService<FavouriteMovie, FavouriteMovieForOwnerViewDTO> favouritesService;
    private final AbstractLinkedCollectionService<CartItem, CartMovieForOwnerViewDTO> cartService;


    public void updateBadges(UserForOwnerViewDTO userForOwnerViewDTO, Model model) {
        if (userForOwnerViewDTO == null) {
            model.addAttribute("cartCount", 0);
            model.addAttribute("favouritesCount", 0);
        } else {
            model.addAttribute("cartCount", cartService
                    .getCount(userForOwnerViewDTO.username()));
            model.addAttribute("favouritesCount", favouritesService
                    .getCount(userForOwnerViewDTO.username()));
        }
    }
}