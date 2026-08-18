package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.user.PasswordChangingDTO;
import com.example.demo.dto.user.ProfileSettingsDTO;
import com.example.demo.dto.user.SessionUser;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.SuccessCode;
import com.example.demo.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PersonalAccountController {

    private final UserService userService;

    @GetMapping("/personal-account")
    public String getPersonalAccount(@SessionAttribute SessionUser sessionUser, Model model) {
        Long userId = sessionUser.id();
        UserForOwnerViewDTO updatedUser = userService.getUserForOwner(userId);
        model.addAttribute("userForOwner", updatedUser);
        model.addAttribute("purchasesCount", userService.getPurchasesCount(userId));
        model.addAttribute("ratingsCount", userService.getRatingsCount(userId));
        return "personal-account";
    }

    @GetMapping("/profile/settings")
    public String getSettingsForm(@SessionAttribute SessionUser sessionUser, Model model) {
        Long userId = sessionUser.id();
        if (!model.containsAttribute("profileSettingsDTO")) {
            model.addAttribute("profileSettingsDTO", userService.getProfileSettings(userId));
        }
        model.addAttribute("wallet", userService.getWalletForOwner(userId));
        return "settings";
    }

    @PostMapping("/profile/settings/change")
    public String changeProfileForm(@Valid @ModelAttribute("profileSettingsDTO") ProfileSettingsDTO profileSettingsDTO,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    @SessionAttribute SessionUser sessionUser,
                                    HttpSession httpSession) {
        Long userId = sessionUser.id();
        if (bindingResult.hasErrors()) {
            return "settings";
        }
        ProfileSettingsDTO normalizedDto = userService.prepareAndValidateProfile(profileSettingsDTO, userId);
        userService.saveProfile(normalizedDto, sessionUser.id());
        SessionUser updatedSessionUser = sessionUser.withFirstLetter(normalizedDto.username());
        httpSession.setAttribute("sessionUser", updatedSessionUser);
        redirectAttributes.addFlashAttribute("successMessage",
                SuccessCode.PROFILE_HAS_BEEN_CHANGED_SUCCESSFULLY.format());
        return "redirect:/profile/settings";
    }

    @PostMapping(value = "/profile/settings/change-ajax", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<ApiResponse> changeProfileAjax(@Valid @RequestBody ProfileSettingsDTO profileSettingsDTO,
                                                         @SessionAttribute SessionUser sessionUser,
                                                         HttpSession httpSession) {
        Long userId = sessionUser.id();
        ProfileSettingsDTO normalizedDto = userService.prepareAndValidateProfile(profileSettingsDTO, userId);
        userService.saveProfile(normalizedDto, userId);
        SessionUser updatedSessionUser = sessionUser.withFirstLetter(normalizedDto.username());
        httpSession.setAttribute("sessionUser", updatedSessionUser);
        return ResponseEntity.ok(ApiResponse.successWithData(
                SuccessCode.PROFILE_HAS_BEEN_CHANGED_SUCCESSFULLY,
                Map.of(
                        "newUsername", profileSettingsDTO.username(),
                        "newEmail", profileSettingsDTO.email(),
                        "newPhone", profileSettingsDTO.phone() == null ? "" : profileSettingsDTO.phone()
                ),
                profileSettingsDTO.username()
        ));
    }

    @GetMapping("/profile/settings/delete/phone")
    @ResponseBody
    public ResponseEntity<ApiResponse> deletePhone(@SessionAttribute SessionUser sessionUser) {
        Long userId = sessionUser.id();
        userService.deletePhone(userId);
        return ResponseEntity.ok(ApiResponse.success(
                SuccessCode.PHONE_HAS_BEEN_REMOVED_SUCCESSFULLY));
    }

    @GetMapping("/profile/settings/change/pwd")
    public String getChangePasswordForm(Model model) {
        if (!model.containsAttribute("passwordChangingDTO")) {
            model.addAttribute("passwordChangingDTO", PasswordChangingDTO.builder().build());
        }
        return "change-password";
    }

    @PostMapping("/profile/settings/change/pwd")
    public String changePassword(@Valid @ModelAttribute("passwordChangingDTO") PasswordChangingDTO passwordChangingDTO,
                                 BindingResult bindingResult,
                                 @SessionAttribute SessionUser sessionUser,
                                 RedirectAttributes redirectAttributes) {
        Long userId = sessionUser.id();
        if (bindingResult.hasErrors()) {
            return "change-password";
        }
        String newPasswordHash = userService.prepareAndValidatePassword(passwordChangingDTO, userId);
        userService.updatePassword(newPasswordHash, userId);
        redirectAttributes.addFlashAttribute("successMessage",
                SuccessCode.PASSWORD_HAS_BEEN_CHANGED_SUCCESSFULLY.format(""));
        return "redirect:/profile/settings/change/pwd";
    }

    @PostMapping("/profile/delete")
    public String deleteAccount(@SessionAttribute SessionUser sessionUser,
                                HttpSession httpSession,
                                RedirectAttributes redirectAttributes) {
        Long userId = sessionUser.id();
        userService.deleteAccount(userId);
        httpSession.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "Аккаунт успешно удален");
        return "redirect:/login";
    }
}