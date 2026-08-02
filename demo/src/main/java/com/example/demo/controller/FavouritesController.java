package com.example.demo.controller;

import com.example.demo.dto.joined_to_user.FavouriteMovieForOwnerViewDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.model.FavouriteMovie;
import com.example.demo.service.AbstractLinkedCollectionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/favourites")
public class FavouritesController extends AbstractLinkedCollectionController<FavouriteMovieForOwnerViewDTO> {


    public FavouritesController(BadgeUpdater badgeUpdater,
                                AbstractLinkedCollectionService<FavouriteMovie, FavouriteMovieForOwnerViewDTO> favouritesService) {
        super(favouritesService, badgeUpdater);
    }

    @GetMapping
    public String getFavourites(Model model,
                                @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO) {
        model.addAttribute("favourites", linkedCollectionService.getAllOfUser(userForOwnerViewDTO.username()));
        model.addAttribute("favouritesCount", linkedCollectionService.getCount(userForOwnerViewDTO.username())); // Вернули счётчик
        badgeUpdater.updateBadges(userForOwnerViewDTO, model);
        return "favourites";
    }
}