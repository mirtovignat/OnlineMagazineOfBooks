package com.example.demo.controller;

import com.example.demo.dto.joined_to_user.HistoricalMovieForOwnerViewDTO;
import com.example.demo.dto.joined_to_user.LibrarianMovieForOwnerViewDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.SuccessCode;
import com.example.demo.service.PurchasedService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/orders")
@AllArgsConstructor
public class PurchasesController {
    private final PurchasedService purchasedService;
    private final BadgeUpdater badgeUpdater;

    @PostMapping("/add/bulk")
    public String buyMovies(RedirectAttributes redirectAttributes,
                            @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO) {
        try {
            purchasedService.validateBulkPurchase(userForOwnerViewDTO.username());
            purchasedService.purchase(userForOwnerViewDTO.username());
            redirectAttributes.addFlashAttribute("successMessage",
                    SuccessCode.PURCHASED_SUCCESSFULLY.format("Все товары"));
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/add/{id}")
    @ResponseBody
    public Map<String, String> buyMovie(
            @PathVariable("id") Long id,
            @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO) {

        purchasedService.validatePurchase(id, userForOwnerViewDTO.username());
        purchasedService.purchase(id, userForOwnerViewDTO.username());

        return Map.of(
                "message",
                SuccessCode.PURCHASED_SUCCESSFULLY.format()
        );
    }

    @GetMapping("/history")
    public String getHistory(Model model,
                             @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
                             @PageableDefault(size = 12, sort = "movie.releaseDate",
                                     direction = Sort.Direction.DESC) Pageable pageable) {
        Page<HistoricalMovieForOwnerViewDTO> history = purchasedService.getHistory(pageable, userForOwnerViewDTO.username());
        model.addAttribute("history", history);
        model.addAttribute("historyCount", history.getTotalElements());
        badgeUpdater.updateBadges(userForOwnerViewDTO, model);
        return "history";
    }

    @GetMapping("/library")
    public String getLibrary(Model model,
                             @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
                             @PageableDefault(size = 12, sort = "movie.releaseDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<LibrarianMovieForOwnerViewDTO> library = purchasedService.getLibrary(pageable, userForOwnerViewDTO.username());
        model.addAttribute("library", library);
        model.addAttribute("libraryCount", library.getTotalElements());
        badgeUpdater.updateBadges(userForOwnerViewDTO, model);
        return "library";
    }
}