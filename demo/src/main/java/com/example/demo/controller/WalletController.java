package com.example.demo.controller;

import com.example.demo.dto.user.SessionUser;
import com.example.demo.dto.wallet.TopUpFormDTO;
import com.example.demo.exception.SuccessCode;
import com.example.demo.model.User;
import com.example.demo.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/wallet")
@AllArgsConstructor
public class WalletController {

    private final UserService userService;

    @GetMapping("/top-up")
    public String getTopUpForm(@SessionAttribute SessionUser sessionUser, Model model) {
        Long userId = sessionUser.id();
        if (!model.containsAttribute("topUpFormDTO")) {
            model.addAttribute("topUpFormDTO", new TopUpFormDTO(null));
        }
        model.addAttribute("wallet", userService.getWalletForOwner(userId));
        return "top-up";
    }

    @PostMapping("/top-up")
    public String topUp(@Valid @ModelAttribute("topUpFormDTO") TopUpFormDTO topUpFormDTO,
                        BindingResult bindingResult,
                        @SessionAttribute SessionUser sessionUser,
                        HttpSession httpSession,
                        RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "top-up";
        }
        User user = userService.getUser(sessionUser.id());
        userService.validateTopUp(topUpFormDTO, user);
        userService.saveTopUp(topUpFormDTO.amount(), user);
        BigDecimal newBalance = userService.getBalance(sessionUser.id());
        SessionUser updated = sessionUser.withBalance(newBalance);
        httpSession.setAttribute("sessionUser", updated);

        redirectAttributes.addFlashAttribute("successMessage",
                SuccessCode.BALANCE_HAS_BEEN_TOPPED_UP_SUCCESSFULLY.format(topUpFormDTO.amount()));
        return "redirect:/wallet/top-up";
    }
}