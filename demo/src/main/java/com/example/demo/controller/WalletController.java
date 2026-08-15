package com.example.demo.controller;

import com.example.demo.dto.user.UserForOwnerViewDTO;
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
        model.addAttribute("wallet", userService.getWalletForOwner(
                userForOwnerViewDTO.username()));
        return "top-up";
    }

    @PostMapping("/top-up")
    public String topUp(@Valid @ModelAttribute("topUpFormDTO") TopUpFormDTO topUpFormDTO,
                        BindingResult bindingResult,
                        @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
                        HttpSession httpSession,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "top-up";
        }
        User user = userService.getUser(userForOwnerViewDTO.username());
        userService.validateTopUp(topUpFormDTO, user);
        userService.saveTopUp(topUpFormDTO.amount(), user);
        UserForOwnerViewDTO freshData = userService.getUserForOwner(userForOwnerViewDTO.username());
        httpSession.setAttribute("userForOwnerViewDTO", freshData);
        redirectAttributes.addFlashAttribute("successMessage",
                SuccessCode.BALANCE_HAS_BEEN_TOPPED_UP_SUCCESSFULLY.format(topUpFormDTO.amount()));
        return "redirect:/wallet/top-up";
    }
}