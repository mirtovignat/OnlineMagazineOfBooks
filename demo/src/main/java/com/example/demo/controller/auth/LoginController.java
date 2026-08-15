package com.example.demo.controller.auth;

import com.example.demo.dto.authorize.Identifier;
import com.example.demo.dto.authorize.LoginFormDTO;
import com.example.demo.dto.badges.BadgeCountsDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.model.User;
import com.example.demo.service.BadgeService;
import com.example.demo.service.user.AuthorizeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

    private final AuthorizeService authorizeService;
    private final BadgeService badgeService;

    @GetMapping
    public String loginPage(HttpSession httpSession, Model model) {
        if (httpSession.getAttribute("userForOwnerViewDTO") != null) {
            return "redirect:/";
        }
        if (!model.containsAttribute("loginForm")) {
            LoginFormDTO loginForm = LoginFormDTO.builder()
                    .identifier(Identifier.USERNAME)
                    .identifierValue("")
                    .rawPassword("")
                    .build();
            model.addAttribute("loginForm", loginForm);
        }
        return "login";
    }

    @PostMapping
    public String login(@Valid @ModelAttribute("loginForm")
                        LoginFormDTO loginFormDTO,
                        HttpSession httpSession) {
        User user = authorizeService.validateLogin(loginFormDTO);
        UserForOwnerViewDTO userForOwnerViewDTO = authorizeService.login(user);
        httpSession.setAttribute("userForOwnerViewDTO", userForOwnerViewDTO);
        try {
            BadgeCountsDTO badges = badgeService.getBadgeCounts(userForOwnerViewDTO.username());
            httpSession.setAttribute("badges", badges);
        } catch (Exception e) {
            httpSession.setAttribute("badges", BadgeCountsDTO.empty());
        }
        return "redirect:/";
    }
}