package com.example.demo.controller.authorize;

import com.example.demo.dto.authorize.RegisterFormDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.authorize.AlreadyRegisteredException;
import com.example.demo.exception.authorize.PasswordsMismatchException;
import com.example.demo.service.AuthorizeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private final AuthorizeService authorizeService;

    @GetMapping
    public String getRegisterForm(Model model) {
        model.addAttribute("registerForm", new
                RegisterFormDTO("", "", ""
                , "", "", "", "",
                "", ""));
        return "register";
    }

    @PostMapping
    public String register(@Valid @ModelAttribute("registerForm")
                           RegisterFormDTO registerFormDTO,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           HttpSession httpSession
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            authorizeService.validateRegister(registerFormDTO);
            UserForOwnerViewDTO userForOwnerViewDTO =
                    authorizeService.register(registerFormDTO);
            httpSession.setAttribute("userForOwnerViewDTO", userForOwnerViewDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Регистрация прошла успешно!");
            return "redirect:/user/movies";
        } catch (PasswordsMismatchException e) {
            redirectAttributes.addFlashAttribute(
                    "registerForm",
                    registerFormDTO);
            redirectAttributes.addFlashAttribute(
                    "passwordsMismatchExceptionMessage",
                    e.getMessage());
            return "redirect:/register";
        } catch (AlreadyRegisteredException e) {
            redirectAttributes.addFlashAttribute("registerForm",
                    registerFormDTO);
            redirectAttributes.addFlashAttribute(
                    "alreadyRegisteredExceptionMessage",
                    e.getMessage());
            return "redirect:/register";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("registerForm",
                    registerFormDTO);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка регистрации");
            return "redirect:/register";
        }
    }
}
