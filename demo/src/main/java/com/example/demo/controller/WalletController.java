package com.example.demo.controller;

import com.example.demo.dto.user.SessionUser;
import com.example.demo.dto.wallet.TopUpFormDTO;
import com.example.demo.exception.SuccessCode;
import com.example.demo.model.entity.User;
import com.example.demo.service.user.UserCommandService;
import com.example.demo.service.user.UserQueryService;
import com.example.demo.web.util.ReturnUrlHelper;
import jakarta.servlet.http.HttpServletRequest;
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

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    @GetMapping("/top-up")
    public String getTopUpForm(HttpServletRequest request,
                               HttpSession session,
                               ReturnUrlHelper returnUrlHelper,
                               @SessionAttribute SessionUser sessionUser,
                               Model model) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            returnUrlHelper.saveReturnUrl(session, referer);
        }
        Long userId = sessionUser.id();
        if (!model.containsAttribute("topUpFormDTO")) {
            model.addAttribute("topUpFormDTO", TopUpFormDTO.builder().build());
        }
        model.addAttribute("wallet", userQueryService.getWalletForOwner(userId));
        return "top-up";
    }

    @PostMapping("/top-up")
    public String topUp(@Valid @ModelAttribute("topUpFormDTO") TopUpFormDTO topUpFormDTO,
                        BindingResult bindingResult,
                        @SessionAttribute SessionUser sessionUser,
                        HttpSession httpSession,
                        RedirectAttributes redirectAttributes,
                        ReturnUrlHelper returnUrlHelper) {
        if (bindingResult.hasErrors()) {
            return "top-up";
        }
        User user = userQueryService.getUser(sessionUser.id());
        userCommandService.saveTopUp(topUpFormDTO.amount(), user);
        BigDecimal newBalance = userQueryService.getBalance(sessionUser.id());
        SessionUser updated = sessionUser.withBalance(newBalance);
        httpSession.setAttribute("sessionUser", updated);
        redirectAttributes.addFlashAttribute("successMessage",
                SuccessCode.BALANCE_HAS_BEEN_TOPPED_UP.getMessage());
        String redirectUrl = returnUrlHelper.getReturnUrlOrDefault(httpSession, "/wallet/top-up");
        return "redirect:" + redirectUrl;
    }
}