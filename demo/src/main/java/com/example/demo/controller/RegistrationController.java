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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@RequestMapping("/register")
public class RegistrationController {
    private final AuthorizeService authorizeService;

    @GetMapping
    public String getRegisterForm(Model model) {
        model.addAttribute("registerForm",
                new RegisterFormDTO("", "", "", "", "", "", "", "", "RUB"));
        return "register";
    }

    @PostMapping
    public String register(@Valid @ModelAttribute("registerForm") RegisterFormDTO registerFormDTO,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes,
                           HttpSession httpSession) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("registerForm", sanitize(registerFormDTO));
            return "register";
        }
        try {
            authorizeService.validateRegister(registerFormDTO);
            UserForOwnerViewDTO userForOwnerViewDTO = authorizeService.register(registerFormDTO);
            httpSession.setAttribute("userForOwnerViewDTO", userForOwnerViewDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Регистрация прошла успешно!");
            return "redirect:/";
        } catch (BusinessException e) {
            ErrorCode errorCode = e.getErrorCode();
            redirectAttributes.addFlashAttribute("registerForm", sanitize(registerFormDTO));

            if (errorCode == ErrorCode.PASSWORDS_MISMATCH) {
                redirectAttributes.addFlashAttribute("passwordsMismatchExceptionMessage", e.getMessage());
            } else if (errorCode == ErrorCode.ALREADY_REGISTERED) {
                redirectAttributes.addFlashAttribute("alreadyRegisteredExceptionMessage", e.getMessage());
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            }
            return "redirect:/register";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("registerForm", sanitize(registerFormDTO));
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка регистрации");
            return "redirect:/register";
        }
    }

    private RegisterFormDTO sanitize(RegisterFormDTO registerFormDTO) {
        return new RegisterFormDTO(
                registerFormDTO.username(),
                registerFormDTO.email(),
                "",
                "",
                registerFormDTO.phone(),
                registerFormDTO.surname(),
                registerFormDTO.name(),
                registerFormDTO.patronymic(),
                registerFormDTO.currencyCode()
        );
    }
}