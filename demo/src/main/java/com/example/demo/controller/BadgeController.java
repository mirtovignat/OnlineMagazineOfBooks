package com.example.demo.controller;

import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.service.BadgeService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/badges")
@AllArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping
    public ResponseEntity<Map<String, Integer>> getBadges(HttpSession session) {
        if (session == null) {
            return ResponseEntity.ok(badgeService.getDefaultBadges());
        }

        UserForOwnerViewDTO user = (UserForOwnerViewDTO) session.getAttribute("userForOwnerViewDTO");
        if (user == null) {
            return ResponseEntity.ok(badgeService.getDefaultBadges());
        }

        return ResponseEntity.ok(badgeService.getBadgeCounts(user.username()));
    }
}