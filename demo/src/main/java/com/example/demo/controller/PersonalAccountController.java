package com.example.demo.controller;

import com.example.demo.dto.joined_to_user.HistoricalMovieForOwnerViewDTO;
import com.example.demo.dto.joined_to_user.RatedMovieForOwnerViewDTO;
import com.example.demo.dto.user.*;
import com.example.demo.exception.user.*;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.PurchasedService;
import com.example.demo.service.RatedService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@AllArgsConstructor
@RequestMapping
public class PersonalAccountController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final UserMapper userMapper;
    private final PurchasedService purchasedService;
    private final RatedService ratedService;

    @GetMapping("/rated-history")
    public String getRated(Model model,
                           @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
                           @PageableDefault(size = 12,
                                   sort = "movie.releaseDate",
                                   direction = Sort.Direction.DESC) Pageable pageable) {
        Page<RatedMovieForOwnerViewDTO> ratedHistory =
                ratedService.getRatedHistory(pageable, userForOwnerViewDTO.username());
        model.addAttribute("ratedHistory", ratedHistory);
        model.addAttribute("ratedCount", ratedHistory.getSize());
        return "for_user/for_owner/rated-history";
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
                Page<HistoricalMovieForOwnerViewDTO> history = purchasedService.getHistory(pageable, userForOwnerViewDTO.username());
                model.addAttribute("history", history);
                model.addAttribute("historyCount", history.getSize());
                return "for_user/for_owner/history";
            }
        } catch (NotAuthorizedUserException e) {
            redirectAttributes.addFlashAttribute("notAuthorizedUserExceptionMessage", e.getMessage());
        }
        return "redirect:" + httpServletRequest.getHeader("Referer");
    }

    @GetMapping("/personal-account")
    public String openPersonalAccount(Model model,
                                      @SessionAttribute(required = false)
                                      UserForOwnerViewDTO userForOwnerViewDTO,
                                      RedirectAttributes redirectAttributes,
                                      HttpServletRequest httpServletRequest) {
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

    @GetMapping("/profile/delete")
    public String deleteAccount(@SessionAttribute("userForOwnerViewDTO")
                                UserForOwnerViewDTO userForOwnerViewDTO,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest httpServletRequest) {
        if (userForOwnerViewDTO == null) {
            redirectAttributes.addFlashAttribute(
                    "notAuthorizedUserExceptionMessage",
                    new NotAuthorizedUserException().getMessage());
            return "redirect:" + httpServletRequest.getHeader("Referer");
        } else {
            userService.deleteAccount(userForOwnerViewDTO.username());
            return "redirect:/movies";
        }
    }

    @PostMapping(value = "/profile/settings/change/username", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<?> changeUsernameAjax(@Valid @RequestBody UsernameChangingDTO usernameChangingDTO,
                                                @SessionAttribute("userForOwnerViewDTO")
                                                UserForOwnerViewDTO userForOwnerViewDTO,
                                                HttpSession session) {
        try {
            User user = userService.findUserByUsername(userForOwnerViewDTO.username());
            userService.changeUsername(usernameChangingDTO, user);
            session.setAttribute("userForOwnerViewDTO", userMapper.toOwnerView(user));
            return ResponseEntity.ok(Map.of("message", "Никнейм успешно обновлен!", "newValue", usernameChangingDTO.username()));
        } catch (DataCoincidenceException | AlreadyRegisteredException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(value = "/profile/settings/change/email", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<?> changeEmailAjax(@Valid @RequestBody EmailChangingDTO emailChangingDTO,
                                             @SessionAttribute("userForOwnerViewDTO")
                                             UserForOwnerViewDTO userForOwnerViewDTO,
                                             HttpSession session) {
        try {
            User user = userService.findUserByUsername(userForOwnerViewDTO.username());
            userService.changeEmail(emailChangingDTO, user);
            session.setAttribute("userForOwnerViewDTO", userMapper.toOwnerView(user));
            return ResponseEntity.ok(Map.of("message", "Почта успешно обновлен!", "newValue", emailChangingDTO.email()));
        } catch (DataCoincidenceException | AlreadyRegisteredException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(value = "/profile/settings/change/phone", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<?> changePhoneAjax(@Valid @RequestBody PhoneChangingDTO phoneChangingDTO,
                                             @SessionAttribute("userForOwnerViewDTO") UserForOwnerViewDTO userForOwnerViewDTO,
                                             HttpSession session) {
        try {
            User user = userService.findUserByUsername(userForOwnerViewDTO.username());
            userService.changePhone(phoneChangingDTO, user);
            session.setAttribute("userForOwnerViewDTO", userMapper.toOwnerView(user));
            return ResponseEntity.ok(Map.of("message", "Телефон успешно обновлен!", "newValue", phoneChangingDTO.phone()));
        } catch (DataCoincidenceException | AlreadyRegisteredException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
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
            ProfileSettingsDTO profileSettingsDTO = userMapper.toSettingsForm(
                    userRepository.findByUsernameOrThrow(userForOwnerViewDTO.username()));
            model.addAttribute("profileSettingsDTO", profileSettingsDTO);
            return "for_user/for_owner/settings";
        }
    }

    @GetMapping("/profile/settings/change/pwd")
    public String getPwdChangingForm(Model model) {
        model.addAttribute("passwordChangingDTO", new PasswordChangingDTO("", "", ""));
        return "for_user/for_owner/change-password";
    }

    @PostMapping("/profile/settings/change/pwd")
    public String changePwd(@Valid @ModelAttribute("passwordChangingDTO")
                            PasswordChangingDTO passwordChangingDTO,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            @SessionAttribute(required = false)
                            UserForOwnerViewDTO userForOwnerViewDTO,
                            HttpSession httpSession) {
        if (bindingResult.hasErrors()) {
            return "for_user/for_owner/change-password";
        }
        User user;
        try {
            user = userService.findUserByUsername(userForOwnerViewDTO.username());
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("notAuthorizedUserExceptionMessage", e.getMessage());
            return "redirect:/login";
        }
        try {
            userService.changePassword(passwordChangingDTO, user);
            httpSession.setAttribute("userForOwnerViewDTO", userMapper.toOwnerView(user));
            redirectAttributes.addFlashAttribute("successMessage", "Пароль успешно обновлен!");
            return "redirect:/personal-account";
        } catch (InvalidPasswordException e) {
            redirectAttributes.addFlashAttribute("invalidPasswordExceptionMessage",
                    e.getMessage());
            redirectAttributes.addFlashAttribute("passwordChangingDTO", passwordChangingDTO);
            return "redirect:/profile/settings/change/pwd";
        } catch (DataCoincidenceException e) {
            redirectAttributes.addFlashAttribute("infoMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("passwordChangingDTO", passwordChangingDTO);
            return "redirect:/profile/settings/change/pwd";
        } catch (AlreadyRegisteredException e) {
            redirectAttributes.addFlashAttribute("passwordChangingDTO", passwordChangingDTO);
            redirectAttributes.addFlashAttribute("alreadyRegisteredExceptionMessage", e.getMessage());
            return "redirect:/profile/settings/change/pwd";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("passwordChangingDTO", passwordChangingDTO);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при изменении настроек");
            return "redirect:/profile/settings/change/pwd";
        }
    }

    @PostMapping("/profile/settings/change")
    public String settings(@Valid @ModelAttribute("profileSettingsDTO") ProfileSettingsDTO profileSettingsDTO,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO,
                           HttpSession httpSession) {
        if (bindingResult.hasErrors()) {
            return "for_user/for_owner/settings";
        }
        User user;
        try {
            user = userService.findUserByUsername(userForOwnerViewDTO.username());
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("notAuthorizedUserExceptionMessage", e.getMessage());
            return "redirect:/login";
        }
        try {
            userService.changeProfile(profileSettingsDTO, user);
            httpSession.setAttribute("userForOwnerViewDTO", userMapper.toOwnerView(user));
            redirectAttributes.addFlashAttribute("successMessage", "Профиль успешно обновлен!");
            return "redirect:/personal-account";
        } catch (DataCoincidenceException e) {
            redirectAttributes.addFlashAttribute("infoMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("profileSettingsDTO", profileSettingsDTO);
            return "redirect:/profile/settings";
        } catch (AlreadyRegisteredException e) {
            redirectAttributes.addFlashAttribute("profileSettingsDTO", profileSettingsDTO);
            redirectAttributes.addFlashAttribute("alreadyRegisteredExceptionMessage", e.getMessage());
            return "redirect:/profile/settings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("profileSettingsDTO", profileSettingsDTO);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при изменении настроек");
            return "redirect:/profile/settings";
        }
    }

    @PostMapping("/profile/settings/change/username")
    public String changeUsername(@Valid @ModelAttribute("usernameChangingDTO") UsernameChangingDTO usernameChangingDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO,
                                 HttpSession httpSession) {
        if (bindingResult.hasErrors()) {
            return "for_user/for_owner/settings";
        }
        User user;
        try {
            user = userService.findUserByUsername(userForOwnerViewDTO.username());
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("notAuthorizedUserExceptionMessage", e.getMessage());
            return "redirect:/login";
        }
        try {
            userService.changeUsername(usernameChangingDTO, user);
            httpSession.setAttribute("userForOwnerViewDTO", userMapper.toOwnerView(user));
            redirectAttributes.addFlashAttribute("successMessage", "Никнейм успешно обновлен!");
            return "redirect:/personal-account";
        } catch (DataCoincidenceException e) {
            redirectAttributes.addFlashAttribute("infoMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("usernameChangingDTO", usernameChangingDTO);
            return "redirect:/profile/settings";
        } catch (AlreadyRegisteredException e) {
            redirectAttributes.addFlashAttribute("usernameChangingDTO", usernameChangingDTO);
            redirectAttributes.addFlashAttribute("alreadyRegisteredExceptionMessage", e.getMessage());
            return "redirect:/profile/settings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("usernameChangingDTO", usernameChangingDTO);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при изменении настроек");
            return "redirect:/profile/settings";
        }
    }

    @PostMapping("/profile/settings/change/email")
    public String changeEmail(@Valid @ModelAttribute("emailChangingDTO") EmailChangingDTO emailChangingDTO,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO,
                              HttpSession httpSession) {
        if (bindingResult.hasErrors()) {
            return "for_user/for_owner/settings";
        }
        User user;
        try {
            user = userService.findUserByUsername(userForOwnerViewDTO.username());
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("notAuthorizedUserExceptionMessage", e.getMessage());
            return "redirect:/login";
        }
        try {
            userService.changeEmail(emailChangingDTO, user);
            httpSession.setAttribute("userForOwnerViewDTO", userMapper.toOwnerView(user));
            redirectAttributes.addFlashAttribute("successMessage", "Почта успешно обновлена!");
            return "redirect:/personal-account";
        } catch (DataCoincidenceException e) {
            redirectAttributes.addFlashAttribute("infoMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("emailChangingDTO", emailChangingDTO);
            return "redirect:/profile/settings";
        } catch (AlreadyRegisteredException e) {
            redirectAttributes.addFlashAttribute("emailChangingDTO", emailChangingDTO);
            redirectAttributes.addFlashAttribute("alreadyRegisteredExceptionMessage", e.getMessage());
            return "redirect:/profile/settings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("emailChangingDTO", emailChangingDTO);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при изменении настроек");
            return "redirect:/profile/settings";
        }
    }

    @PostMapping("/profile/settings/change/phone")
    public String changePhone(@Valid @ModelAttribute("phoneChangingDTO") PhoneChangingDTO phoneChangingDTO,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO,
                              HttpSession httpSession) {
        if (bindingResult.hasErrors()) {
            return "for_user/for_owner/settings";
        }
        User user;
        try {
            user = userService.findUserByUsername(userForOwnerViewDTO.username());
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("notAuthorizedUserExceptionMessage", e.getMessage());
            return "redirect:/login";
        }
        try {
            userService.changePhone(phoneChangingDTO, user);
            httpSession.setAttribute("userForOwnerViewDTO", userMapper.toOwnerView(user));
            redirectAttributes.addFlashAttribute("successMessage", "Номер телефона успешно обновлен!");
            return "redirect:/personal-account";
        } catch (DataCoincidenceException e) {
            redirectAttributes.addFlashAttribute("infoMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("phoneChangingDTO", phoneChangingDTO);
            return "redirect:/profile/settings";
        } catch (AlreadyRegisteredException e) {
            redirectAttributes.addFlashAttribute("phoneChangingDTO", phoneChangingDTO);
            redirectAttributes.addFlashAttribute("alreadyRegisteredExceptionMessage", e.getMessage());
            return "redirect:/profile/settings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("phoneChangingDTO", phoneChangingDTO);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при изменении настроек");
            return "redirect:/profile/settings";
        }
    }

    @GetMapping("/profile/settings/delete/phone")
    public String deletePhone(@SessionAttribute("userForOwnerViewDTO")
                              UserForOwnerViewDTO userForOwnerViewDTO,
                              RedirectAttributes redirectAttributes,
                              HttpServletRequest httpServletRequest,
                              HttpSession session) {
        if (userForOwnerViewDTO == null) {
            redirectAttributes.addFlashAttribute("notAuthorizedUserExceptionMessage",
                    new NotAuthorizedUserException().getMessage());
            return "redirect:" + httpServletRequest.getHeader("Referer");
        }
        try {
            userService.deletePhone(userForOwnerViewDTO.username());
            User user = userService.findUserByUsername(userForOwnerViewDTO.username());
            session.setAttribute("userForOwnerViewDTO", userMapper.toOwnerView(user));
            redirectAttributes.addFlashAttribute("successMessage", "Номер телефона удалён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении номера телефона");
        }
        return "redirect:" + httpServletRequest.getHeader("Referer");
    }
}