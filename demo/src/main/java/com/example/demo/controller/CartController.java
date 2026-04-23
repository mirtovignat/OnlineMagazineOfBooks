package com.example.demo.controller;

import com.example.demo.dto.joined_to_user.CartMovieForOwnerViewDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.cart.EmptyException;
import com.example.demo.exception.user.NotAuthorizedUserException;
import com.example.demo.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
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
    public int getCartCount(@SessionAttribute("userForOwnerViewDTO") UserForOwnerViewDTO userForOwnerViewDTO) {
        return cartService.getCartCount(userForOwnerViewDTO.username());
    }

    @GetMapping
    public String getCart(Model model,
                          HttpServletRequest httpServletRequest,
                          RedirectAttributes redirectAttributes,
                          @SessionAttribute(required = false)
                          UserForOwnerViewDTO userForOwnerViewDTO) {
        if (userForOwnerViewDTO == null) {
            redirectAttributes.addFlashAttribute(
                    "notAuthorizedUserExceptionMessage",
                    new NotAuthorizedUserException().getMessage());
            return "redirect:" + httpServletRequest.getHeader("Referer");
        } else {
            List<CartMovieForOwnerViewDTO> cart = cartService.getAllInCartOfUser(
                    userForOwnerViewDTO.username()
            );
            model.addAttribute("cart", cart);
            badgeUpdater.updateBadges(userForOwnerViewDTO, model);
            return "for_user/for_owner/cart";
        }
    }

    @PostMapping("/add/{title}")
    public String addToCart(RedirectAttributes redirectAttributes,
                            @PathVariable("title") String title,
                            @SessionAttribute(required = false)
                            UserForOwnerViewDTO userForOwnerViewDTO,
                            HttpServletRequest httpServletRequest) {
        if (userForOwnerViewDTO == null) {
            redirectAttributes.addFlashAttribute(
                    "notAuthorizedUserExceptionMessage",
                    new NotAuthorizedUserException().getMessage());
        } else {
            cartService.addToCart(title, userForOwnerViewDTO.username());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Товар успешно добавлен в корзину!");
        }
        return "redirect:" + httpServletRequest.getHeader("Referer");
    }

    @GetMapping("/remove/{title}")
    public String removeFromCart(HttpServletRequest httpServletRequest,
                                 RedirectAttributes redirectAttributes,
                                 @PathVariable("title") String title,
                                 @SessionAttribute(required = false)
                                 UserForOwnerViewDTO userForOwnerViewDTO) {
        if (userForOwnerViewDTO == null) {
            NotAuthorizedUserException e = new NotAuthorizedUserException();
            redirectAttributes.addFlashAttribute(
                    "notAuthorizedUserExceptionMessage", e.getMessage());
        } else {
            cartService.removeFromCart(title, userForOwnerViewDTO.username());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Товар убран из корзины!");
        }
        return "redirect:" + httpServletRequest.getHeader("Referer");
    }

    @GetMapping("/clear")
    public String removeAllFromCart(RedirectAttributes redirectAttributes,
                                    @SessionAttribute(required = false)
                                    UserForOwnerViewDTO userForOwnerViewDTO,
                                    HttpServletRequest httpServletRequest) {
        try {
            cartService.removeAllFromCart(userForOwnerViewDTO.username());
            redirectAttributes.addFlashAttribute("successMessage", "Корзина очищена!");
        } catch (NotAuthorizedUserException e) {
            redirectAttributes.addFlashAttribute("notAuthorizedUserExceptionMessage", e.getMessage());
        } catch (EmptyException e) {
            redirectAttributes.addFlashAttribute("emptyCartExceptionMessage", e.getMessage());
        }
        return "redirect:" + httpServletRequest.getHeader("Referer");
    }
}