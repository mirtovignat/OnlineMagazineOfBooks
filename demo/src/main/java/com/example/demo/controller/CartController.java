package com.example.demo.controller;

import com.example.demo.dto.joined_to_user.CartMovieForOwnerViewDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.service.CartService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@AllArgsConstructor
public class CartController {
    private final BadgeUpdater badgeUpdater;
    private final CartService cartService;

    @GetMapping("/count")
    @ResponseBody
    public int getCartCount(@SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO) {
        return userForOwnerViewDTO == null ? 0 : cartService.getCartCount(userForOwnerViewDTO.username());
    }

    @GetMapping
    public String getCart(Model model, @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO) {
        List<CartMovieForOwnerViewDTO> cart = cartService.getAllInCartOfUser(userForOwnerViewDTO.username());
        model.addAttribute("cart", cart);
        badgeUpdater.updateBadges(userForOwnerViewDTO, model);
        return "cart";
    }

    @PostMapping("/clear")
    @ResponseBody
    public ResponseEntity<Map<String, String>> removeAllFromCart(@SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO) {
        if (userForOwnerViewDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Пожалуйста, авторизуйтесь"));
        }
        cartService.removeAllFromCart(userForOwnerViewDTO.username());
        return ResponseEntity.ok(Map.of("message", "Корзина успешно очищена!"));
    }

    @PostMapping("/add/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> addToCart(@PathVariable("id") Long id,
                                                         @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO) {
        if (userForOwnerViewDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Не авторизован"));
        }
        cartService.addToCart(id, userForOwnerViewDTO.username());
        return ResponseEntity.ok(Map.of("message", "Товар добавлен в корзину"));
    }

    @PostMapping("/remove/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> removeFromCart(@PathVariable("id") Long id,
                                                              @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO) {
        if (userForOwnerViewDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Не авторизован"));
        }
        cartService.removeFromCart(id, userForOwnerViewDTO.username());
        return ResponseEntity.ok(Map.of("message", "Товар убран из корзины"));
    }
}