package com.example.demo.controller;

import com.example.demo.dto.authorize.LoginFormDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.model.User;
import com.example.demo.service.AuthorizeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@RequestMapping("/login")
public class LoginController {
    private final AuthorizeService authorizeService;

    @GetMapping
    public String loginPage(Model model) {
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", LoginFormDTO.builder().build());
        }
        return "login";
    }

    @PostMapping
    public String login(@Valid @ModelAttribute("loginForm") LoginFormDTO loginFormDTO,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        HttpSession httpSession,
                        Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("loginForm", sanitize(loginFormDTO));
            return "login";
        }
        try {
            User user = authorizeService.validateLogin(loginFormDTO);
            UserForOwnerViewDTO userForOwnerViewDTO = authorizeService.login(user);
            httpSession.setAttribute("userForOwnerViewDTO", userForOwnerViewDTO);
            return "redirect:/";
        } catch (BusinessException e) {
            ErrorCode errorCode = e.getErrorCode();
            if (errorCode == ErrorCode.PASSWORD_INVALID) {
                redirectAttributes.addFlashAttribute("invalidPasswordExceptionMessage", e.getMessage());
            } else if (errorCode == ErrorCode.USER_NOT_FOUND) {
                redirectAttributes.addFlashAttribute("userNotFoundExceptionMessage", e.getMessage());
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            }
            redirectAttributes.addFlashAttribute("loginForm", sanitize(loginFormDTO));
            return "redirect:/login";
        }
    }

    private LoginFormDTO sanitize(LoginFormDTO loginFormDTO) {
        return new LoginFormDTO(loginFormDTO.identifier(), loginFormDTO.identifierValue(), "");
    }
}