package com.example.demo.controller.linked_collection;

import com.example.demo.dto.catalog.CartMovieForOwnerViewDTO;
import com.example.demo.dto.user.SessionUser;
import com.example.demo.service.linked_collection.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/cart")
public class CartController extends AbstractLinkedCollectionController<CartMovieForOwnerViewDTO> {

    public CartController(CartService cartService) {
        super(cartService);
    }

    @GetMapping
    public String getCart(Model model,
                          @SessionAttribute SessionUser sessionUser) {
        Long userId = sessionUser.id();
        model.addAttribute("cart", linkedCollectionService.getAllOfUser(userId));
        model.addAttribute("cartCount", linkedCollectionService.getCount(userId));
        return "cart";
    }
}