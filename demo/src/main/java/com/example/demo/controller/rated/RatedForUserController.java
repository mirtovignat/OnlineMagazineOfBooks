package com.example.demo.controller.rated;

import com.example.demo.dto.joined_to_user.RatedMovieForOwnerFormDTO;
import com.example.demo.dto.joined_to_user.RatedMovieForOwnerViewDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.authorize.NotAuthorizedUserException;
import com.example.demo.service.RatedService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@AllArgsConstructor
@Controller
@RequestMapping("/user/rated")
public class RatedForUserController {
    @Autowired
    private final RatedService ratedService;

    @GetMapping("/{title}/reviews")
    public String getMovieReviews(HttpServletRequest httpServletRequest,
                                  Model model,
                                  @PathVariable("title") String title,
                                  @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO,
                                  RedirectAttributes redirectAttributes) {

        if (userForOwnerViewDTO == null) {
            redirectAttributes.addFlashAttribute(
                    "notAuthorizedUserExceptionMessage",
                    new NotAuthorizedUserException().getMessage());
            return "redirect:" + httpServletRequest.getHeader("Referer");
        } else {
            model.addAttribute("currentUsername",
                    userForOwnerViewDTO.username());
            model.addAttribute("isRatedByCurrentUser",
                    ratedService.isRatedByUser(title,
                            userForOwnerViewDTO.username()));
            model.addAttribute("reviews",
                    ratedService.getMovieReviewsForUser(
                            title)
            );
            return "for_user/movie/reviews";
        }
    }

    @PostMapping("/add/{title}")
    public String rate(Model model, HttpServletRequest httpServletRequest,
                       RedirectAttributes redirectAttributes,
                       @ModelAttribute("ratedMovieForOwnerFormDTO")
                       RatedMovieForOwnerFormDTO ratedMovieForOwnerFormDTO,
                       @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO) {
        if (userForOwnerViewDTO == null) {
            redirectAttributes.addFlashAttribute(
                    "notAuthorizedUserExceptionMessage",
                    new NotAuthorizedUserException().getMessage());
            return "redirect:" + httpServletRequest.getHeader("Referer");
        } else {
            RatedMovieForOwnerViewDTO ratedMovieForOwnerViewDTO =
                    ratedService.rate(ratedMovieForOwnerFormDTO,
                            userForOwnerViewDTO.username());
            model.addAttribute("ratedMovieForOwnerViewDTO",
                    ratedMovieForOwnerViewDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Отзыв оставлен!");
        }

        return "redirect:" + httpServletRequest.getHeader("Referer");
    }

    @PostMapping("/edit/{title}")
    public String editRating(Model model, HttpServletRequest httpServletRequest,
                             RedirectAttributes redirectAttributes,
                             @ModelAttribute("ratedMovieForOwnerFormDTO")
                             RatedMovieForOwnerFormDTO ratedMovieForOwnerFormDTO,
                             @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO) {

        if (userForOwnerViewDTO == null) {
            redirectAttributes.addFlashAttribute(
                    "notAuthorizedUserExceptionMessage",
                    new NotAuthorizedUserException().getMessage());
            return "redirect:" + httpServletRequest.getHeader("Referer");
        } else {
            RatedMovieForOwnerViewDTO ratedMovieForOwnerViewDTO =
                    ratedService.updateRating(ratedMovieForOwnerFormDTO,
                            userForOwnerViewDTO.username());
            model.addAttribute("ratedMovieForOwnerViewDTO",
                    ratedMovieForOwnerViewDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Отзыв изменен!");
        }
        return "redirect:" + httpServletRequest.getHeader("Referer");
    }

    @PostMapping("/remove/{title}")
    public String removeFromRated(RedirectAttributes redirectAttributes,
                                  @PathVariable("title") String title,
                                  @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO,
                                  HttpServletRequest httpServletRequest) {
        try {
            if (userForOwnerViewDTO == null) {
                redirectAttributes.addFlashAttribute(
                        "notAuthorizedUserExceptionMessage",
                        new NotAuthorizedUserException().getMessage());
                return "redirect:" + httpServletRequest.getHeader("Referer");
            } else {
                ratedService.removeRating(title,
                        userForOwnerViewDTO.username());
                redirectAttributes.addFlashAttribute("successMessage",
                        "Отзыв удален!");
            }
        } catch (NotAuthorizedUserException e) {
            redirectAttributes.addFlashAttribute(
                    "notAuthorizedUserExceptionMessage",
                    e.getMessage());
        }
        return "redirect:" + httpServletRequest.getHeader("Referer");
    }

}
