package com.example.demo.controller;

import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.authorize.NotAuthorizedUserException;
import com.example.demo.exception.cart.EmptyException;
import com.example.demo.service.FavouritesService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user/favourites")
@AllArgsConstructor
public class FavouritesController {

    private final FavouritesService favouriteService;
    private final BadgeUpdater badgeUpdater;

    @GetMapping("/count")
    @ResponseBody
    public int getFavouritesCount(@SessionAttribute("userForOwnerViewDTO")
                                  UserForOwnerViewDTO userForOwnerViewDTO) {
        return favouriteService.getFavouritesCount(userForOwnerViewDTO.username());
    }

    @PostMapping("/add/{title}")
    public String addToFavourites(HttpServletRequest httpServletRequest,
                                  RedirectAttributes redirectAttributes,
                                  @PathVariable String title,
                                  @SessionAttribute(required = false)
                                  UserForOwnerViewDTO userForOwnerViewDTO) {
        if (userForOwnerViewDTO == null) {
            redirectAttributes.addFlashAttribute("notAuthorizedUserExceptionMessage", new NotAuthorizedUserException().getMessage());
        } else {
            favouriteService.addToFavourites(title, userForOwnerViewDTO.username());
            redirectAttributes.addFlashAttribute("successMessage", "Добавлено в избранное!");
        }
        return "redirect:" + httpServletRequest.getHeader("Referer");
    }

    @GetMapping("/remove/{title}")
    public String removeFromFavourites(HttpServletRequest httpServletRequest,
                                       RedirectAttributes redirectAttributes,
                                       @PathVariable String title,
                                       @SessionAttribute(required = false)
                                       UserForOwnerViewDTO userForOwnerViewDTO) {
        if (userForOwnerViewDTO == null) {
            redirectAttributes.addFlashAttribute("notAuthorizedUserExceptionMessage", new NotAuthorizedUserException().getMessage());
        } else {
            favouriteService.removeFromFavourites(title, userForOwnerViewDTO.username());
            redirectAttributes.addFlashAttribute("successMessage", "Удалено из избранного!");
        }
        return "redirect:" + httpServletRequest.getHeader("Referer");
    }

    @GetMapping("/clear")
    public String clearFavourites(HttpServletRequest httpServletRequest,
                                  RedirectAttributes redirectAttributes,
                                  @SessionAttribute(required = false)
                                  UserForOwnerViewDTO userForOwnerViewDTO) {
        try {
            if (userForOwnerViewDTO == null) throw new NotAuthorizedUserException();
            favouriteService.removeAllFromFavourites(userForOwnerViewDTO.username());
            redirectAttributes.addFlashAttribute("successMessage", "Избранное очищено!");
        } catch (NotAuthorizedUserException e) {
            redirectAttributes.addFlashAttribute("notAuthorizedUserExceptionMessage", e.getMessage());
        } catch (EmptyException e) {
            redirectAttributes.addFlashAttribute("emptyCartExceptionMessage", e.getMessage());
        }
        return "redirect:" + httpServletRequest.getHeader("Referer");
    }

    @GetMapping
    public String getFavourites(HttpServletRequest httpServletRequest,
                                Model model,
                                @SessionAttribute(required = false)
                                UserForOwnerViewDTO userForOwnerViewDTO,
                                RedirectAttributes ra) {
        if (userForOwnerViewDTO == null) {
            ra.addFlashAttribute("notAuthorizedUserExceptionMessage", new NotAuthorizedUserException().getMessage());
            return "redirect:" + httpServletRequest.getHeader("Referer");
        }
        model.addAttribute("favourites", favouriteService.getAllInFavouritesOfUser(userForOwnerViewDTO.username()));
        badgeUpdater.updateBadges(userForOwnerViewDTO, model);
        return "for_user/for_owner/favourites";
    }
}