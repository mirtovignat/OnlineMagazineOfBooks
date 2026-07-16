package com.example.demo.controller;

import com.example.demo.dto.user.PasswordChangingDTO;
import com.example.demo.dto.user.ProfileSettingsDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@AllArgsConstructor
public class PersonalAccountController {

    private final UserService userService;
    private final BadgeUpdater badgeUpdater;

    @GetMapping("/personal-account")
    public String getPersonalAccount(@SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO,
                                     Model model) {
        if (userForOwnerViewDTO == null) {
            return "redirect:/login";
        }
        UserForOwnerViewDTO updatedUser = userService.getUserForOwner(userForOwnerViewDTO.username());
        model.addAttribute("userForOwner", updatedUser);
        badgeUpdater.updateBadges(updatedUser, model);
        return "personal-account";
    }

    @GetMapping("/profile/settings")
    public String getSettingsForm(@SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
                                  Model model) {
        if (!model.containsAttribute("profileSettingsDTO")) {
            ProfileSettingsDTO profileSettingsDTO = userService.getProfileSettings(userForOwnerViewDTO.username());
            model.addAttribute("profileSettingsDTO", profileSettingsDTO);
        }
        model.addAttribute("wallet", userService.getWalletForOwner(userForOwnerViewDTO.username()));
        badgeUpdater.updateBadges(userForOwnerViewDTO, model);
        return "settings";
    }

    @PostMapping("/profile/settings/change")
    public String changeProfileForm(@Valid @ModelAttribute("profileSettingsDTO") ProfileSettingsDTO profileSettingsDTO,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
                                    HttpSession httpSession,
                                    Model model) {
        if (bindingResult.hasErrors()) {
            badgeUpdater.updateBadges(userForOwnerViewDTO, model);
            return "settings";
        }
        try {
            userService.changeProfile(profileSettingsDTO, userForOwnerViewDTO.username());
            // Обновляем сессию
            UserForOwnerViewDTO updated = userService.getUserForOwner(profileSettingsDTO.username());
            httpSession.setAttribute("userForOwnerViewDTO", updated);
            redirectAttributes.addFlashAttribute("successMessage", "Профиль успешно обновлен!");
            return "redirect:/personal-account";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("profileSettingsDTO", profileSettingsDTO);
            return "redirect:/profile/settings";
        }
    }

    @PostMapping(value = "/profile/settings/change-ajax", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, String>> changeProfileAjax(@Valid @RequestBody ProfileSettingsDTO profileSettingsDTO,
                                                                 @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
                                                                 HttpSession httpSession) {
        try {
            userService.changeProfile(profileSettingsDTO, userForOwnerViewDTO.username());
            UserForOwnerViewDTO updated = userService.getUserForOwner(profileSettingsDTO.username());
            httpSession.setAttribute("userForOwnerViewDTO", updated);
            return ResponseEntity.ok(Map.of(
                    "message", "Изменения успешно сохранены!",
                    "newUsername", profileSettingsDTO.username(),
                    "newEmail", profileSettingsDTO.email(),
                    "newPhone", profileSettingsDTO.phone() == null ? "" : profileSettingsDTO.phone()
            ));
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/profile/settings/delete/phone")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deletePhone(@SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
                                                           HttpSession httpSession) {
        userService.deletePhone(userForOwnerViewDTO.username());
        UserForOwnerViewDTO updated = userService.getUserForOwner(userForOwnerViewDTO.username());
        httpSession.setAttribute("userForOwnerViewDTO", updated);
        return ResponseEntity.ok(Map.of("message", "Телефон успешно удален"));
    }

    @GetMapping("/profile/settings/change/pwd")
    public String getChangePasswordForm(Model model) {
        if (!model.containsAttribute("passwordChangingDTO")) {
            model.addAttribute("passwordChangingDTO", new PasswordChangingDTO("", "", ""));
        }
        return "change-password";
    }

    @PostMapping("/profile/settings/change/pwd")
    public String changePassword(@Valid @ModelAttribute("passwordChangingDTO") PasswordChangingDTO passwordChangingDTO,
                                 BindingResult bindingResult,
                                 @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "change-password";
        }
        try {
            userService.changePassword(passwordChangingDTO, userForOwnerViewDTO.username());
            redirectAttributes.addFlashAttribute("successMessage", "Пароль успешно изменен!");
            return "redirect:/personal-account";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("passwordChangingDTO", passwordChangingDTO);
            return "redirect:/profile/settings/change/pwd";
        }
    }

    @PostMapping("/profile/delete")
    public String deleteAccount(@SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        userService.deleteAccount(userForOwnerViewDTO.username());
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "Аккаунт успешно удален");
        return "redirect:/login";
    }
}