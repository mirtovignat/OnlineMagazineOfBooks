package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.user.PasswordChangingDTO;
import com.example.demo.dto.user.ProfileSettingsDTO;
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
    public String getPersonalAccount(@SessionAttribute(required = false)
                                     UserForOwnerViewDTO userForOwnerViewDTO,
                                     Model model) {
        if (userForOwnerViewDTO == null) {
            return "redirect:/login";
        }
        UserForOwnerViewDTO updatedUser = userService
                .getUserForOwner(userForOwnerViewDTO.username());
        model.addAttribute("userForOwner", updatedUser);
        model.addAttribute("purchasesCount",
                userService.getPurchasesCount(updatedUser.username()));
        model.addAttribute("ratingsCount",
                userService.getRatingsCount(updatedUser.username()));
        return "personal-account";
    }

    @GetMapping("/profile/settings")
    public String getSettingsForm(@SessionAttribute
                                  UserForOwnerViewDTO userForOwnerViewDTO,
                                  Model model) {
        if (!model.containsAttribute("profileSettingsDTO")) {
            model.addAttribute("profileSettingsDTO",
                    userService.getProfileSettings(userForOwnerViewDTO.username()));
        }
        model.addAttribute("wallet",
                userService.getWalletForOwner(userForOwnerViewDTO.username()));
        return "settings";
    }

    @PostMapping("/profile/settings/change")
    public String changeProfileForm(@Valid @ModelAttribute("profileSettingsDTO")
                                    ProfileSettingsDTO profileSettingsDTO,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
                                    HttpSession httpSession) {
        if (bindingResult.hasErrors()) {
            return "settings";
        }
        ProfileSettingsDTO normalizedDto = userService
                .prepareAndValidateProfile(profileSettingsDTO,
                        userForOwnerViewDTO.username());
        userService.saveProfile(normalizedDto, userForOwnerViewDTO.username());
        UserForOwnerViewDTO updatedUser = syncUserInSession(normalizedDto.username(), httpSession);
        redirectAttributes.addFlashAttribute("successMessage",
                SuccessCode.PROFILE_HAS_BEEN_CHANGED_SUCCESSFULLY.format(updatedUser.username()));
        return "redirect:/personal-account";
    }

    @PostMapping(value = "/profile/settings/change-ajax", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<ApiResponse> changeProfileAjax(@Valid @RequestBody
                                                         ProfileSettingsDTO profileSettingsDTO,
                                                         @SessionAttribute
                                                         UserForOwnerViewDTO userForOwnerViewDTO,
                                                         HttpSession httpSession) {
        ProfileSettingsDTO normalizedDto = userService.prepareAndValidateProfile(profileSettingsDTO,
                userForOwnerViewDTO.username());
        userService.saveProfile(normalizedDto, userForOwnerViewDTO.username());
        syncUserInSession(normalizedDto.username(), httpSession);
        return ResponseEntity.ok(ApiResponse.successWithData(
                SuccessCode.PROFILE_HAS_BEEN_CHANGED_SUCCESSFULLY,
                Map.of(
                        "newUsername", normalizedDto.username(),
                        "newEmail", normalizedDto.email(),
                        "newPhone", normalizedDto.phone() == null ? "" : normalizedDto.phone()
                ),
                normalizedDto.username()
        ));
    }

    @GetMapping("/profile/settings/delete/phone")
    @ResponseBody
    public ResponseEntity<ApiResponse> deletePhone(@SessionAttribute
                                                   UserForOwnerViewDTO userForOwnerViewDTO,
                                                   HttpSession httpSession) {
        userService.deletePhone(userForOwnerViewDTO.username());
        syncUserInSession(userForOwnerViewDTO.username(), httpSession);
        return ResponseEntity.ok(ApiResponse.success(
                SuccessCode.PHONE_HAS_BEEN_REMOVED_SUCCESSFULLY, ""));
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
        String newPasswordHash = userService.prepareAndValidatePassword(
                passwordChangingDTO, userForOwnerViewDTO.username());
        userService.savePassword(newPasswordHash, userForOwnerViewDTO.username());
        redirectAttributes.addFlashAttribute("successMessage",
                SuccessCode.PASSWORD_HAS_BEEN_CHANGED_SUCCESSFULLY.format(""));
        return "redirect:/personal-account";
    }

    @PostMapping("/profile/delete")
    public String deleteAccount(@SessionAttribute
                                UserForOwnerViewDTO userForOwnerViewDTO,
                                HttpSession httpSession,
                                RedirectAttributes redirectAttributes) {
        userService.deleteAccount(userForOwnerViewDTO.username());
        httpSession.invalidate();
        redirectAttributes.addFlashAttribute("successMessage",
                "Аккаунт успешно удален");
        return "redirect:/login";
    }

    private UserForOwnerViewDTO syncUserInSession(String username,
                                                  HttpSession httpSession) {
        UserForOwnerViewDTO freshData = userService.getUserForOwner(username);
        httpSession.setAttribute("userForOwnerViewDTO", freshData);
        return freshData;
    }
}