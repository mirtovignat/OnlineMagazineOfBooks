package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.user.PasswordChangingDTO;
import com.example.demo.dto.user.ProfileSettingsDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.SuccessCode;
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
            UserForOwnerViewDTO updated = userService.getUserForOwner(profileSettingsDTO.username());
            httpSession.setAttribute("userForOwnerViewDTO", updated);
            redirectAttributes.addFlashAttribute("successMessage",
                    SuccessCode.PROFILE_HAS_BEEN_CHANGED_SUCCESSFULLY.format(updated.username()));
            return "redirect:/personal-account";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("profileSettingsDTO", profileSettingsDTO);
            return "redirect:/profile/settings";
        }
    }

    @PostMapping(value = "/profile/settings/change-ajax", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<ApiResponse> changeProfileAjax(@Valid @RequestBody
                                                         ProfileSettingsDTO profileSettingsDTO,
                                                         @SessionAttribute
                                                         UserForOwnerViewDTO userForOwnerViewDTO,
                                                         HttpSession httpSession) {
        try {
            userService.changeProfile(profileSettingsDTO, userForOwnerViewDTO.username());
            UserForOwnerViewDTO updated = userService.getUserForOwner(profileSettingsDTO.username());
            httpSession.setAttribute("userForOwnerViewDTO", updated);

            return ResponseEntity.ok(ApiResponse.successWithData(
                    SuccessCode.PROFILE_HAS_BEEN_CHANGED_SUCCESSFULLY,
                    Map.of(
                            "newUsername", profileSettingsDTO.username(),
                            "newEmail", profileSettingsDTO.email(),
                            "newPhone", profileSettingsDTO.phone() == null ? "" : profileSettingsDTO.phone()
                    ),
                    profileSettingsDTO.username()
            ));
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @GetMapping("/profile/settings/delete/phone")
    @ResponseBody
    public ResponseEntity<ApiResponse> deletePhone(@SessionAttribute
                                                   UserForOwnerViewDTO userForOwnerViewDTO,
                                                   HttpSession httpSession) {
        userService.deletePhone(userForOwnerViewDTO.username());
        UserForOwnerViewDTO updated = userService.getUserForOwner(userForOwnerViewDTO.username());
        httpSession.setAttribute("userForOwnerViewDTO", updated);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.PHONE_HAS_BEEN_REMOVED_SUCCESSFULLY, ""));
    }

    @GetMapping("/profile/settings/change/pwd")
    public String getChangePasswordForm(Model model) {
        if (!model.containsAttribute("passwordChangingDTO")) {
            model.addAttribute("passwordChangingDTO",
                    PasswordChangingDTO.builder().build());
        }
        return "change-password";
    }

    @PostMapping("/profile/settings/change/pwd")
    public String changePassword(@Valid @ModelAttribute("passwordChangingDTO")
                                 PasswordChangingDTO passwordChangingDTO,
                                 BindingResult bindingResult,
                                 @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "change-password";
        }
        try {
            userService.changePassword(passwordChangingDTO, userForOwnerViewDTO.username());
            redirectAttributes.addFlashAttribute("successMessage",
                    SuccessCode.PASSWORD_HAS_BEEN_CHANGED_SUCCESSFULLY.format(""));
            return "redirect:/personal-account";
        } catch (BusinessException businessException) {
            redirectAttributes.addFlashAttribute("errorMessage", businessException.getMessage());
            redirectAttributes.addFlashAttribute("passwordChangingDTO", passwordChangingDTO);
            return "redirect:/profile/settings/change/pwd";
        }
    }

    @PostMapping("/profile/delete")
    public String deleteAccount(@SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
                                HttpSession httpSession,
                                RedirectAttributes redirectAttributes) {
        userService.deleteAccount(userForOwnerViewDTO.username());
        httpSession.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "Аккаунт успешно удален");
        return "redirect:/login";
    }
}