package com.example.demo.controller.linked_collection;

import com.example.demo.dto.catalog.FavouriteMovieForOwnerViewDTO;
import com.example.demo.dto.user.SessionUser;
import com.example.demo.service.linked_collection.FavouritesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/favourites")
public class FavouritesController extends AbstractLinkedCollectionController<FavouriteMovieForOwnerViewDTO> {

    public FavouritesController(FavouritesService favouritesService) {
        super(favouritesService);
    }

    @GetMapping
    public String getFavourites(Model model,
                                @SessionAttribute SessionUser sessionUser) {
        Long userId = sessionUser.id();
        model.addAttribute("favourites", linkedCollectionService.getAllOfUser(userId));
        model.addAttribute("favouritesCount", linkedCollectionService.getCount(userId));
        return "favourites";
    }
}