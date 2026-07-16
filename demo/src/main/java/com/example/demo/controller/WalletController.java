package com.example.demo.controller;

import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.dto.wallet.TopUpFormDTO;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BadgeUpdater badgeUpdater;

    @GetMapping("/top-up")
    public String getTopUpForm(@SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
                               Model model) {
        if (!model.containsAttribute("topUpFormDTO")) {
            model.addAttribute("topUpFormDTO", new TopUpFormDTO(null));
        }
        model.addAttribute("wallet", userService.getWalletForOwner(userForOwnerViewDTO.username()));
        badgeUpdater.updateBadges(userForOwnerViewDTO, model);
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
            User updated = userRepository.findByUsernameOrThrow(userForOwnerViewDTO.username());
            httpSession.setAttribute("userForOwnerViewDTO", userMapper.toOwnerView(updated));
            redirectAttributes.addFlashAttribute("successMessage", "Счёт успешно пополнен!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/wallet/top-up";
    }
}