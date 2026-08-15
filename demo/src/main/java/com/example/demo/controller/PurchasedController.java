package com.example.demo.controller;

import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.SuccessCode;
import com.example.demo.service.purchased.PurchasedCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class PurchasedController {

    private final PurchasedCommandService purchasedCommandService;

    @PostMapping("/add/bulk")
    @ResponseBody
    public ResponseEntity<Map<String, String>> buyMovies(
            @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO) {
        purchasedCommandService.purchaseBulk(userForOwnerViewDTO.username());
        return ResponseEntity.ok(Map.of(
                "message", SuccessCode.PURCHASED_SUCCESSFULLY.format()
        ));
    }

    @PostMapping("/add/{id}")
    @ResponseBody
    public Map<String, String> buyMovie(@PathVariable Long id,
                                        @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO) {
        purchasedCommandService.purchase(id, userForOwnerViewDTO.username());
        return Map.of("message", SuccessCode.PURCHASED_SUCCESSFULLY.format());
    }
}