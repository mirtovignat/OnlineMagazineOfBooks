package com.example.demo.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class FallbackController {

    @GetMapping
    public String rootRedirect() {
        return "redirect:/movies";
    }

    @GetMapping("/logout")
    public String logOut() {
        return "redirect:/movies";
    }

}