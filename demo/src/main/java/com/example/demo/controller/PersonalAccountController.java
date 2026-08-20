package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.dto.user.PasswordChangingDTO;
import com.example.demo.dto.user.ProfileSettingsDTO;
import com.example.demo.dto.user.SessionUser;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.SuccessCode;
import com.example.demo.service.user.UserCommandService;
import com.example.demo.service.user.UserQueryService;
import com.example.demo.web.util.ReturnUrlHelper;
import jakarta.servlet.http.HttpServletRequest;
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

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @GetMapping("/personal-account")
    public String getPersonalAccount(@SessionAttribute SessionUser sessionUser, Model model) {
        Long userId = sessionUser.id();
        UserForOwnerViewDTO updatedUser = userQueryService.getUserForOwner(userId);
        UserResponse response = userQueryService.buildUserPage(updatedUser);
        model.addAttribute("response", response);
        return "personal-account";
    }

    @GetMapping("/profile/settings")
    public String getSettingsForm(@SessionAttribute SessionUser sessionUser,
                                  HttpServletRequest request,
                                  HttpSession session,
                                  ReturnUrlHelper returnUrlHelper,
                                  Model model) {
        Long userId = sessionUser.id();
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            returnUrlHelper.saveReturnUrl(session, referer);
        }

        if (!model.containsAttribute("profileSettingsDTO")) {
            model.addAttribute("profileSettingsDTO", userQueryService.getProfileSettings(userId));
        }
        model.addAttribute("wallet", userQueryService.getWalletForOwner(userId));
        return "settings";
    }

    @PostMapping("/profile/settings/change")
    public String changeProfileForm(@Valid @ModelAttribute("profileSettingsDTO") ProfileSettingsDTO profileSettingsDTO,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    @SessionAttribute SessionUser sessionUser,
                                    HttpSession httpSession,
                                    ReturnUrlHelper returnUrlHelper) {
        Long userId = sessionUser.id();
        if (bindingResult.hasErrors()) {
            return "settings";
        }
        ProfileSettingsDTO normalizedDto = userCommandService.prepareAndValidateProfile(profileSettingsDTO, userId);
        userCommandService.saveProfile(normalizedDto, userId);
        SessionUser updatedSessionUser = sessionUser.withFirstLetter(normalizedDto.username());
        httpSession.setAttribute("sessionUser", updatedSessionUser);
        redirectAttributes.addFlashAttribute("successMessage",
                SuccessCode.PROFILE_HAS_BEEN_CHANGED.getMessage());

        String redirectUrl = returnUrlHelper.getReturnUrlOrDefault(httpSession, "/profile/settings");
        return "redirect:" + redirectUrl;
    }

    @PostMapping(value = "/profile/settings/change-ajax", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<ApiResponse> changeProfileAjax(@Valid @RequestBody ProfileSettingsDTO profileSettingsDTO,
                                                         @SessionAttribute SessionUser sessionUser,
                                                         HttpSession httpSession) {
        Long userId = sessionUser.id();
        ProfileSettingsDTO normalizedDto = userCommandService.prepareAndValidateProfile(profileSettingsDTO, userId);
        userCommandService.saveProfile(normalizedDto, userId);
        SessionUser updatedSessionUser = sessionUser.withFirstLetter(normalizedDto.username());
        httpSession.setAttribute("sessionUser", updatedSessionUser);
        return ResponseEntity.ok(ApiResponse.builder()
                .message(SuccessCode.PROFILE_HAS_BEEN_CHANGED.getMessage())
                .data(Map.of(
                        "newUsername", profileSettingsDTO.username(),
                        "newEmail", profileSettingsDTO.email(),
                        "newPhone", profileSettingsDTO.phone() == null ? "" : profileSettingsDTO.phone()
                ))
                .build());
    }

    @GetMapping("/profile/settings/delete/phone")
    @ResponseBody
    public ResponseEntity<ApiResponse> deletePhone(@SessionAttribute SessionUser sessionUser) {
        Long userId = sessionUser.id();
        userCommandService.deletePhone(userId);
        return ResponseEntity.ok(ApiResponse.response(
                SuccessCode.PHONE_HAS_BEEN_REMOVED));
    }

    @GetMapping("/profile/settings/change/pwd")
    public String getChangePasswordForm(HttpServletRequest request,
                                        HttpSession session,
                                        ReturnUrlHelper returnUrlHelper,
                                        Model model) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            returnUrlHelper.saveReturnUrl(session, referer);
        }
        if (!model.containsAttribute("passwordChangingDTO")) {
            model.addAttribute("passwordChangingDTO", PasswordChangingDTO.builder().build());
        }
        return "change-password";
    }

    @PostMapping("/profile/settings/change/pwd")
    public String changePassword(@Valid @ModelAttribute("passwordChangingDTO") PasswordChangingDTO passwordChangingDTO,
                                 BindingResult bindingResult,
                                 @SessionAttribute SessionUser sessionUser,
                                 RedirectAttributes redirectAttributes,
                                 HttpSession httpSession,
                                 ReturnUrlHelper returnUrlHelper) {
        Long userId = sessionUser.id();
        if (bindingResult.hasErrors()) {
            return "change-password";
        }
        String newPasswordHash = userCommandService.prepareAndValidatePassword(passwordChangingDTO, userId);
        userCommandService.updatePassword(newPasswordHash, userId);
        redirectAttributes.addFlashAttribute("successMessage",
                SuccessCode.PASSWORD_HAS_BEEN_CHANGED.getMessage());
        String redirectUrl = returnUrlHelper.getReturnUrlOrDefault(httpSession, "/profile/settings/change/pwd");
        return "redirect:" + redirectUrl;
    }

    @PostMapping("/profile/delete")
    public String deleteAccount(@SessionAttribute SessionUser sessionUser,
                                HttpSession httpSession,
                                RedirectAttributes redirectAttributes) {
        Long userId = sessionUser.id();
        userCommandService.deleteAccount(userId);
        httpSession.invalidate();
        redirectAttributes.addFlashAttribute("successMessage",
                SuccessCode.ACCOUNT_HAS_BEEN_REMOVED.getMessage());
        return "redirect:/login";
    }
}