package com.example.demo.controller.authorize;

import com.example.demo.dto.authorize.LoginFormDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.authorize.InvalidPasswordException;
import com.example.demo.exception.authorize.UserNotFoundException;
import com.example.demo.model.User;
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
@RequestMapping("/login")
public class LoginController {
    @Autowired
    private final AuthorizeService authorizeService;

    @GetMapping
    public String loginPage(Model model) {
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm",
                    new LoginFormDTO(null, "", ""));
        }
        return "login";
    }

    @PostMapping
    public String login(@Valid @ModelAttribute("loginForm") LoginFormDTO loginFormDTO,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        HttpSession httpSession, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("loginForm", loginFormDTO);
            return "login";
        }
        try {
            User user = authorizeService.validateLogin(loginFormDTO);
            UserForOwnerViewDTO userForOwnerViewDTO =
                    authorizeService.login(user);
            httpSession.setAttribute("userForOwnerViewDTO", userForOwnerViewDTO);
            return "redirect:/user/movies";
        } catch (InvalidPasswordException e) {
            redirectAttributes.addFlashAttribute(
                    "invalidPasswordExceptionMessage",
                    e.getMessage());
            redirectAttributes.addFlashAttribute(
                    "loginForm", loginFormDTO);
            return "redirect:/login";
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute(
                    "userNotFoundExceptionMessage",
                    e.getMessage());
            redirectAttributes.addFlashAttribute(
                    "loginForm", loginFormDTO);
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "exceptionMessage",
                    "Ошибка!");
            redirectAttributes.addFlashAttribute(
                    "loginForm", loginFormDTO);
            return "redirect:/login";
        }
    }
}
