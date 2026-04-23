package com.example.demo.controller;

import com.example.demo.dto.joined_to_user.LibrarianMovieForOwnerViewDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.purchased.InsufficientFundsException;
import com.example.demo.exception.user.NotAuthorizedUserException;
import com.example.demo.service.PurchasedService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user/purchases")
@AllArgsConstructor
public class PurchasesController {
    private final PurchasedService purchasedService;

    @PostMapping("/add/bulk")
    public String buyMovies(HttpServletRequest httpServletRequest,
                            RedirectAttributes redirectAttributes,
                            @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO) {
        try {
            if (userForOwnerViewDTO == null) {
                redirectAttributes.addFlashAttribute(
                        "notAuthorizedUserExceptionMessage",
                        new NotAuthorizedUserException().getMessage());
                return "redirect:" + httpServletRequest.getHeader("Referer");
            } else {
                purchasedService.purchase(
                        userForOwnerViewDTO.username());
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Покупка прошла успешно!");
            }
        } catch (InsufficientFundsException e) {
            redirectAttributes.addFlashAttribute(
                    "insufficientFundsExceptionMessage",
                    e.getMessage());

        }
        return "redirect:" + httpServletRequest.getHeader("Referer");
    }

    @GetMapping("/add/{title}")
    public String buyMovie(HttpServletRequest httpServletRequest,
                           RedirectAttributes redirectAttributes,
                           @PathVariable("title") String title,
                           @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO) {
        try {
            if (userForOwnerViewDTO == null) {
                redirectAttributes.addFlashAttribute(
                        "notAuthorizedUserExceptionMessage",
                        new NotAuthorizedUserException().getMessage());
                return "redirect:" + httpServletRequest.getHeader("Referer");
            } else {
                purchasedService.purchase(title,
                        userForOwnerViewDTO.username());
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Товар куплен!");
            }
        } catch (InsufficientFundsException e) {
            redirectAttributes.addFlashAttribute(
                    "insufficientFundsExceptionMessage",
                    e.getMessage());
        }
        return "redirect:" + httpServletRequest.getHeader("Referer");
    }


    @GetMapping("/library")
    public String getLibrary(HttpServletRequest httpServletRequest,
                             Model model,
                             @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO,
                             RedirectAttributes redirectAttributes,
                             @PageableDefault(size = 12,
                                     sort = "movie.releaseDate",
                                     direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            if (userForOwnerViewDTO == null) {
                redirectAttributes.addFlashAttribute(
                        "notAuthorizedUserExceptionMessage",
                        new NotAuthorizedUserException().getMessage());
                return "redirect:" + httpServletRequest.getHeader("Referer");
            } else {
                Page<LibrarianMovieForOwnerViewDTO> library =
                        purchasedService
                                .getLibrary(pageable, userForOwnerViewDTO.username()
                                );
                model.addAttribute("library", library);
                model.addAttribute("libraryCount", library.getSize());
                return "for_user/for_owner/library";
            }
        } catch (NotAuthorizedUserException e) {
            redirectAttributes.addFlashAttribute(
                    "notAuthorizedUserExceptionMessage",
                    e.getMessage());
        }
        return "redirect:" + httpServletRequest.getHeader("Referer");
    }


}
