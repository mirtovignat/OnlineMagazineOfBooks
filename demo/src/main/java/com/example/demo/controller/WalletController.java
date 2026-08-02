package com.example.demo.controller;

import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.dto.wallet.TopUpFormDTO;
import com.example.demo.exception.SuccessCode;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/wallet")
@AllArgsConstructor
public class WalletController {

    private final UserService userService;

    @GetMapping("/top-up")
    public String getTopUpForm(@SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
                               Model model) {
        if (!model.containsAttribute("topUpFormDTO")) {
            model.addAttribute("topUpFormDTO", new TopUpFormDTO(null));
        }
        model.addAttribute("wallet", userService.getWalletForOwner(userForOwnerViewDTO.username()));
        return "top-up";
    }

    @PostMapping("/top-up")
    public String topUp(@Valid @ModelAttribute("topUpFormDTO") TopUpFormDTO topUpFormDTO,
                        BindingResult bindingResult,
                        @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
                        HttpSession httpSession,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Сумма должна быть положительной (минимум 0.01)");
            return "redirect:/wallet/top-up";
        }
        User user = userService.findUserByUsername(userForOwnerViewDTO.username());
        try {
            userService.topUp(topUpFormDTO, user);
            UserForOwnerViewDTO freshData = userService.getUserForOwner(userForOwnerViewDTO.username());
            UserForOwnerViewDTO updatedInSession = new UserForOwnerViewDTO(
                    freshData.id(), freshData.username(),
                    userForOwnerViewDTO.cartCount(), userForOwnerViewDTO.favouritesCount(),
                    freshData.email(), freshData.phone(), freshData.balance(),
                    freshData.currencyCode(),
                    userForOwnerViewDTO.purchasesCount(), userForOwnerViewDTO.ratingsCount()
            );
            httpSession.setAttribute("userForOwnerViewDTO", updatedInSession);
            redirectAttributes.addFlashAttribute("successMessage",
                    SuccessCode.BALANCE_HAS_BEEN_TOPPED_UP_SUCCESSFULLY.format(topUpFormDTO.amount()));
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/wallet/top-up";
    }
}