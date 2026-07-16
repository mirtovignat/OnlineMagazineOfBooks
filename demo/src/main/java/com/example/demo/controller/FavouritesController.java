package com.example.demo.controller;

import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.service.FavouritesService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/favourites")
@AllArgsConstructor
public class FavouritesController {
    private final FavouritesService favouriteService;
    private final BadgeUpdater badgeUpdater;

    @GetMapping("/count")
    @ResponseBody
    public int getFavouritesCount(@SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO) {
        return userForOwnerViewDTO == null ? 0 : favouriteService.getFavouritesCount(userForOwnerViewDTO.username());
    }

    @PostMapping("/add/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> addToFavourites(@PathVariable("id") Long id,
                                                               @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO) {
        if (userForOwnerViewDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Не авторизован"));
        }
        favouriteService.addToFavourites(id, userForOwnerViewDTO.username());
        return ResponseEntity.ok(Map.of("message", "Добавлено в избранное"));
    }

    @PostMapping("/remove/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> removeFromFavourites(@PathVariable("id") Long id,
                                                                    @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO) {
        if (userForOwnerViewDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Не авторизован"));
        }
        favouriteService.removeFromFavourites(id, userForOwnerViewDTO.username());
        return ResponseEntity.ok(Map.of("message", "Удалено из избранного"));
    }

    @PostMapping("/clear")
    @ResponseBody
    public ResponseEntity<Map<String, String>> clearFavourites(@SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO) {
        if (userForOwnerViewDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Пожалуйста, авторизуйтесь"));
        }
        favouriteService.removeAllFromFavourites(userForOwnerViewDTO.username());
        return ResponseEntity.ok(Map.of("message", "Избранное успешно очищено!"));
    }

    @GetMapping
    public String getFavourites(Model model, @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO) {
        model.addAttribute("favourites", favouriteService.getAllInFavouritesOfUser(userForOwnerViewDTO.username()));
        badgeUpdater.updateBadges(userForOwnerViewDTO, model);
        return "favourites";
    }
}