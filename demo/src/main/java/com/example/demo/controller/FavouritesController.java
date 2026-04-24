package com.example.demo.controller;

import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.service.FavouritesService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @ResponseBody
    public ResponseEntity<?> addToFavourites(@PathVariable String title,
                                             @SessionAttribute(required = false)
                                             UserForOwnerViewDTO userForOwnerViewDTO) {
        if (userForOwnerViewDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\": \"Не авторизован\"}");
        }
        favouriteService.addToFavourites(title, userForOwnerViewDTO.username());
        return ResponseEntity.ok().body("{\"message\": \"Добавлено в избранное\"}");
    }

    @PostMapping("/remove/{title}")
    @ResponseBody
    public ResponseEntity<?> removeFromFavourites(@PathVariable String title,
                                                  @SessionAttribute(required = false)
                                                  UserForOwnerViewDTO userForOwnerViewDTO) {
        if (userForOwnerViewDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\": \"Не авторизован\"}");
        }
        favouriteService.removeFromFavourites(title, userForOwnerViewDTO.username());
        return ResponseEntity.ok().body("{\"message\": \"Удалено из избранного\"}");
    }

    @PostMapping("/clear")
    public String clearFavourites(HttpServletRequest httpServletRequest,
                                  RedirectAttributes redirectAttributes,
                                  @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO) {
        favouriteService.removeAllFromFavourites(userForOwnerViewDTO.username());
        redirectAttributes.addFlashAttribute("successMessage", "Избранное очищено!");
        return "redirect:" + httpServletRequest.getHeader("Referer");
    }

    @GetMapping
    public String getFavourites(HttpServletRequest httpServletRequest,
                                Model model,
                                @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO) {
        model.addAttribute("favourites", favouriteService.getAllInFavouritesOfUser(userForOwnerViewDTO.username()));
        badgeUpdater.updateBadges(userForOwnerViewDTO, model);
        return "for_user/for_owner/favourites";
    }
}