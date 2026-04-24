package com.example.demo.controller.authorize;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class LogoutController {

    @PostMapping("/logout")
    public void logout(HttpServletRequest httpServletRequest) {
        HttpSession httpSession = httpServletRequest
                .getSession(false);
        if (httpSession != null) {
            httpSession.removeAttribute("userForOwnerViewDTO");
            httpSession.invalidate();
        }
    }
}
