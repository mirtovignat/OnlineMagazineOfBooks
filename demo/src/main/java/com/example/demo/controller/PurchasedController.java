package com.example.demo.controller;

import com.example.demo.dto.user.SessionUser;
import com.example.demo.exception.SuccessCode;
import com.example.demo.service.purchased.PurchasedCommandService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class PurchasedController {

    private final PurchasedCommandService purchasedCommandService;

    @PostMapping("/add/bulk")
    @ResponseBody
    public ResponseEntity<Map<String, String>> buyMovies(
            @SessionAttribute SessionUser sessionUser,
            HttpSession httpSession) {
        Long userId = sessionUser.id();
        BigDecimal newBalance = purchasedCommandService.purchaseBulk(userId);
        SessionUser updated = sessionUser.withBalance(newBalance);
        httpSession.setAttribute("sessionUser", updated);
        return ResponseEntity.ok(Map.of(
                "message", SuccessCode.PURCHASED_SUCCESSFULLY.format()
        ));
    }

    @PostMapping("/add/{id}")
    @ResponseBody
    public Map<String, String> buyMovie(@PathVariable Long id,
                                        @SessionAttribute SessionUser sessionUser,
                                        HttpSession httpSession) {
        Long userId = sessionUser.id();
        BigDecimal newBalance = purchasedCommandService.purchase(id, userId);
        SessionUser updated = sessionUser.withBalance(newBalance);
        httpSession.setAttribute("sessionUser", updated);
        return Map.of("message", SuccessCode.PURCHASED_SUCCESSFULLY.format());
    }
}