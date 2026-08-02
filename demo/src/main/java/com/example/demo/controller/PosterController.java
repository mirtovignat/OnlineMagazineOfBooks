package com.example.demo.controller;

import com.example.demo.service.PosterService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posters")
@RequiredArgsConstructor
public class PosterController {

    @Autowired
    private final PosterService posterService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadPoster(@RequestParam("file")
                                               MultipartFile file) {
        try {
            String url = posterService.uploadPoster(file);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка загрузки: " +
                    e.getMessage());
        }
    }
}