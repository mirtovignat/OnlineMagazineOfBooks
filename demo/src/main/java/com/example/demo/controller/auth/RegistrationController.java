package com.example.demo.controller.auth;

import com.example.demo.dto.authorize.RegisterFormDTO;
import com.example.demo.dto.badges.BadgeCountsDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.service.user.AuthorizeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
                RegisterFormDTO.initial());
        return "register";
    }

    @PostMapping
    public String register(@Valid @ModelAttribute("registerForm")
                           RegisterFormDTO registerFormDTO,
                           HttpSession httpSession) {
        authorizeService.validateRegister(registerFormDTO);
        UserForOwnerViewDTO userForOwnerViewDTO = authorizeService.register(registerFormDTO);
        httpSession.setAttribute("userForOwnerViewDTO", userForOwnerViewDTO);
        httpSession.setAttribute("badges", BadgeCountsDTO.empty());
        return "redirect:/";
    }
}