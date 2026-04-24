package com.example.demo.controller;

import com.example.demo.dto.joined_to_user.CartMovieForOwnerViewDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/user/cart")
@AllArgsConstructor
public class CartController {
    private final BadgeUpdater badgeUpdater;
    private final CartService cartService;

    @GetMapping("/count")
    @ResponseBody
    public int getCartCount(@SessionAttribute("userForOwnerViewDTO")
                            UserForOwnerViewDTO userForOwnerViewDTO) {
        return cartService.getCartCount(userForOwnerViewDTO.username());
    }

    @GetMapping
    public String getCart(Model model,
                          @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO) {
        List<CartMovieForOwnerViewDTO> cart = cartService.getAllInCartOfUser(userForOwnerViewDTO.username());
        model.addAttribute("cart", cart);
        badgeUpdater.updateBadges(userForOwnerViewDTO, model);
        return "for_user/for_owner/cart";
    }

    @PostMapping("/clear")
    public String removeAllFromCart(RedirectAttributes redirectAttributes,
                                    @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
                                    HttpServletRequest httpServletRequest) {
        cartService.removeAllFromCart(userForOwnerViewDTO.username());
        redirectAttributes.addFlashAttribute("successMessage",
                "Корзина очищена!");
        return "redirect:" + httpServletRequest.getHeader("Referer");
    }

    @PostMapping("/add/{title}")
    @ResponseBody
    public ResponseEntity<?> addToCart(@PathVariable("title") String title,
                                       @SessionAttribute(required = false)
                                       UserForOwnerViewDTO userForOwnerViewDTO) {
        if (userForOwnerViewDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\": \"Не авторизован\"}");
        }
        cartService.addToCart(title, userForOwnerViewDTO.username());
        return ResponseEntity.ok().body("{\"message\": \"Товар добавлен в корзину\"}");
    }

    @PostMapping("/remove/{title}")
    @ResponseBody
    public ResponseEntity<?> removeFromCart(@PathVariable("title") String title,
                                            @SessionAttribute(required = false)
                                            UserForOwnerViewDTO userForOwnerViewDTO) {
        if (userForOwnerViewDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\": \"Не авторизован\"}");
        }
        cartService.removeFromCart(title, userForOwnerViewDTO.username());
        return ResponseEntity.ok().body("{\"message\": \"Товар убран из корзины\"}");
    }
}