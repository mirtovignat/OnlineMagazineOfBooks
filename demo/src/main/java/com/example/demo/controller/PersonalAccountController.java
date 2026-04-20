package com.example.demo.controller;

import com.example.demo.dto.joined_to_user.HistoricalMovieForOwnerViewDTO;
import com.example.demo.dto.joined_to_user.RatedMovieForOwnerViewDTO;
import com.example.demo.dto.user.ProfileSettingsDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.authorize.AlreadyRegisteredException;
import com.example.demo.exception.authorize.NotAuthorizedUserException;
import com.example.demo.exception.authorize.PasswordsMismatchException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CartService;
import com.example.demo.service.PurchasedService;
import com.example.demo.service.RatedService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@RequestMapping
public class PersonalAccountController {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final UserService userService;
    @Autowired
    private final UserMapper userMapper;
    @Autowired
    private final PurchasedService purchasedService;
    @Autowired
    private final RatedService ratedService;
    @Autowired
    private final CartService cartService;

    @GetMapping("/rated-history")
    public String getRated(HttpServletRequest httpServletRequest,
                           Model model,
                           @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO,
                           RedirectAttributes redirectAttributes,
                           @PageableDefault(size = 12,
                                   sort = "movie.releaseDate",
                                   direction = Sort.Direction.DESC) Pageable pageable) {


        try {
            if (userForOwnerViewDTO == null) {
                redirectAttributes.addFlashAttribute(
                        "notAuthorizedUserExceptionMessage",
                        new NotAuthorizedUserException().getMessage());
                return "redirect:" + httpServletRequest.getHeader("Referer");
            } else {
                Page<RatedMovieForOwnerViewDTO> ratedHistory =
                        ratedService
                                .getRatedHistory(pageable,
                                        userForOwnerViewDTO.username());
                model.addAttribute("ratedHistory", ratedHistory);
                model.addAttribute("ratedCount",
                        ratedHistory.getSize());
                return "for_user/for_owner/rated_history";
            }
        } catch (NotAuthorizedUserException e) {
            redirectAttributes.addFlashAttribute(
                    "notAuthorizedUserExceptionMessage",
                    e.getMessage());
        }
        return "redirect:" + httpServletRequest.getHeader("Referer");
    }

    @GetMapping("/history")
    public String getHistory(HttpServletRequest httpServletRequest,
                             Model model,
                             @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO,
                             RedirectAttributes redirectAttributes,
                             @PageableDefault(size = 12,
                                     sort = "movie.releaseDate",
                                     direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            if (userForOwnerViewDTO == null) {
                redirectAttributes.addFlashAttribute(
                        "notAuthorizedUserExceptionMessage",
                        new NotAuthorizedUserException().getMessage());
                return "redirect:" + httpServletRequest.getHeader("Referer");
            } else {
                Page<HistoricalMovieForOwnerViewDTO> history = purchasedService
                        .getHistory(pageable,
                                userForOwnerViewDTO.username());
                model.addAttribute("history", history);
                model.addAttribute("historyCount", history.getSize());
                return "for_user/for_owner/history";
            }
        } catch (NotAuthorizedUserException e) {
            redirectAttributes.addFlashAttribute(
                    "notAuthorizedUserExceptionMessage",
                    e.getMessage());
        }
        return "redirect:" + httpServletRequest.getHeader("Referer");
    }

    @GetMapping("/personal-account")
    public String openPersonalAccount(Model model,
                                      @SessionAttribute(required = false)
                                      UserForOwnerViewDTO userForOwnerViewDTO,
                                      RedirectAttributes redirectAttributes,
                                      HttpServletRequest httpServletRequest
    ) {
        if (userForOwnerViewDTO == null) {
            redirectAttributes.addFlashAttribute(
                    "notAuthorizedUserExceptionMessage",
                    new NotAuthorizedUserException().getMessage());
            return "redirect:" + httpServletRequest.getHeader("Referer");
        } else {
            model.addAttribute("userForOwner",
                    userService.getUserForOwner(userForOwnerViewDTO.username()));
            return "personal-account";
        }
    }

    @GetMapping("/profile/settings")
    public String getSettingsForm(@SessionAttribute("userForOwnerViewDTO") UserForOwnerViewDTO userForOwnerViewDTO,
                                  Model model, RedirectAttributes redirectAttributes,
                                  HttpServletRequest httpServletRequest) {
        if (userForOwnerViewDTO == null) {
            redirectAttributes.addFlashAttribute(
                    "notAuthorizedUserExceptionMessage",
                    new NotAuthorizedUserException().getMessage());
            return "redirect:" + httpServletRequest.getHeader("Referer");
        } else {
            ProfileSettingsDTO profileSettingsDTO =
                    userMapper.toSettingsForm(userRepository
                            .findByUsernameOrThrow(userForOwnerViewDTO.username()));
            model.addAttribute("profileSettingsDTO",
                    profileSettingsDTO);
            return "for_user/for_owner/settings";
        }
    }

    @PostMapping("/profile/settings/change")
    public String settings(@Valid @ModelAttribute("profileSettingsDTO")
                           ProfileSettingsDTO profileSettingsDTO,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           HttpServletRequest httpServletRequest,
                           @SessionAttribute(required = false)
                           UserForOwnerViewDTO userForOwnerViewDTO,
                           HttpSession httpSession
    ) {
        if (bindingResult.hasErrors()) {
            return "for_user/for_owner/settings";
        }
        try {
            if (userForOwnerViewDTO == null) {
                redirectAttributes.addFlashAttribute(
                        "notAuthorizedUserExceptionMessage",
                        new NotAuthorizedUserException().getMessage());
                return "redirect:" + httpServletRequest.getHeader("Referer");
            } else {
                userService.changeProfile(
                        profileSettingsDTO, userForOwnerViewDTO.username());
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Профиль успешно обновлен!");

                httpSession.setAttribute("userForOwnerViewDTO",
                        userMapper.toOwnerView(
                                userRepository.findByUsernameOrThrow(
                                        profileSettingsDTO.username())
                        ));
                return "redirect:/personal-account";
            }
        } catch (PasswordsMismatchException e) {
            redirectAttributes.addFlashAttribute(
                    "profileSettingsDTO",
                    profileSettingsDTO);
            redirectAttributes.addFlashAttribute(
                    "passwordsMismatchExceptionMessage",
                    e.getMessage());
        } catch (AlreadyRegisteredException e) {
            redirectAttributes.addFlashAttribute("profileSettingsDTO",
                    profileSettingsDTO);
            redirectAttributes.addFlashAttribute(
                    "alreadyRegisteredExceptionMessage",
                    e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("profileSettingsDTO",
                    profileSettingsDTO);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при изменении настроек");
        }
        return "redirect:/profile/settings";
    }
}
