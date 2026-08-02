package com.example.demo.controller;

import com.example.demo.dto.joined_to_user.CartMovieForOwnerViewDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.model.CartItem;
import com.example.demo.service.AbstractLinkedCollectionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/cart")
public class CartController extends AbstractLinkedCollectionController<CartMovieForOwnerViewDTO> {


    public CartController(BadgeUpdater badgeUpdater,
                          AbstractLinkedCollectionService<CartItem, CartMovieForOwnerViewDTO> cartService) {
        super(cartService, badgeUpdater);
    }

    @GetMapping
    public String getCart(Model model,
                          @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO) {
        model.addAttribute("cart", linkedCollectionService.getAllOfUser(userForOwnerViewDTO.username()));
        model.addAttribute("cartCount", linkedCollectionService.getCount(userForOwnerViewDTO.username())); // Вернули счётчик
        badgeUpdater.updateBadges(userForOwnerViewDTO, model);
        return "cart";
    }
}