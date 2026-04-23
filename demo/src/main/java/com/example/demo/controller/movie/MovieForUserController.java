package com.example.demo.controller.movie;

import com.example.demo.controller.BadgeUpdater;
import com.example.demo.dto.movie.MovieCardDetailsForUserViewDTO;
import com.example.demo.dto.movie.MovieCardForUserViewDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.user.NotAuthorizedUserException;
import com.example.demo.service.MovieService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@RequestMapping("/user/movies")
public class MovieForUserController {

    private final MovieService movieService;
    private final BadgeUpdater badgeUpdater;

    @GetMapping
    public String showMovieForUser(HttpServletRequest httpServletRequest,
                                   RedirectAttributes redirectAttributes,
                                   Model model,
                                   @SessionAttribute(required = false)
                                   UserForOwnerViewDTO userForOwnerViewDTO,
                                   @PageableDefault(size = 12, sort = "releaseDate",
                                           direction = Sort.Direction.DESC)
                                   Pageable pageable
    ) {
        if (userForOwnerViewDTO == null) {
            redirectAttributes.addFlashAttribute(
                    "notAuthorizedUserExceptionMessage",
                    new NotAuthorizedUserException().getMessage());
            return "redirect:" + httpServletRequest.getHeader("Referer");
        } else {
            Page<MovieCardForUserViewDTO> cardsPage = movieService.getMovieCardsForUser(
                    userForOwnerViewDTO.username(), pageable);
            badgeUpdater.updateBadges(userForOwnerViewDTO, model);
            model.addAttribute("cardsPage", cardsPage);
            return "for_user/movie/index";
        }
    }

    @GetMapping("/{title}")
    public String showMovieForUser(RedirectAttributes redirectAttributes,
                                   @PathVariable("title") String title,
                                   Model model,
                                   @SessionAttribute(required = false)
                                   UserForOwnerViewDTO userForOwnerViewDTO,
                                   HttpServletRequest httpServletRequest
    ) {
        if (userForOwnerViewDTO == null) {
            redirectAttributes.addFlashAttribute(
                    "notAuthorizedUserExceptionMessage",
                    new NotAuthorizedUserException().getMessage());
            return "redirect:" + httpServletRequest.getHeader("Referer");
        } else {
            MovieCardDetailsForUserViewDTO card =
                    movieService.getCardForUser(userForOwnerViewDTO.username(),
                            title);
            badgeUpdater.updateBadges(userForOwnerViewDTO, model);
            model.addAttribute("card", card);
            return "for_user/movie/show";
        }
    }
}
