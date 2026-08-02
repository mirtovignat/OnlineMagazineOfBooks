package com.example.demo.controller;

import com.example.demo.dto.authorize.RegisterFormDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
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

@Controller
@AllArgsConstructor
@RequestMapping("/register")
public class RegistrationController {

    private final AuthorizeService authorizeService;

    @GetMapping
    public String getRegisterForm(Model model) {
        model.addAttribute("registerForm",
                RegisterFormDTO.builder().currencyCode("RUB").build());
        return "register";
    }

    @PostMapping
    public String register(@Valid @ModelAttribute("registerForm") RegisterFormDTO registerFormDTO,
                           BindingResult bindingResult,
                           Model model,
                           HttpSession httpSession) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("registerForm", registerFormDTO);
            return "register";
        }
        try {
            authorizeService.validateRegister(registerFormDTO);
            UserForOwnerViewDTO userForOwnerViewDTO = authorizeService.register(registerFormDTO);
            httpSession.setAttribute("userForOwnerViewDTO", userForOwnerViewDTO);
            return "redirect:/";
        } catch (BusinessException businessException) {
            ErrorCode errorCode = businessException.getErrorCode();
            model.addAttribute("registerForm", registerFormDTO);
            if (errorCode == ErrorCode.PASSWORDS_MISMATCH) {
                model.addAttribute("passwordsMismatchExceptionMessage", businessException.getMessage());
            } else if (errorCode == ErrorCode.ALREADY_REGISTERED) {
                model.addAttribute("alreadyRegisteredExceptionMessage", businessException.getMessage());
            } else {
                model.addAttribute("errorMessage", businessException.getMessage());
            }
            return "register";
        } catch (Exception e) {
            model.addAttribute("registerForm", registerFormDTO);
            model.addAttribute("errorMessage", "Ошибка регистрации");
            return "register";
        }
    }
}