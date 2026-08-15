package com.example.demo.controller;

import com.example.demo.service.movie.poster.MoviePosterService;
import com.example.demo.service.movie.poster.PosterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posters")
@RequiredArgsConstructor
public class PosterController {

    private final PosterService posterService;
    private final MoviePosterService moviePosterService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadPoster(
            @RequestParam("file") MultipartFile file) {
        try {
            String url = posterService.uploadPoster(file);
            return ResponseEntity.ok(url);
        } catch (Exception exception) {
            return ResponseEntity
                    .internalServerError()
                    .body("Ошибка загрузки: " + exception.getMessage());
        }
    }

    @PostMapping("/tmdb/{movieId}")
    public ResponseEntity<String> replacePosterFromTMDB(
            @PathVariable Long movieId) {
        try {
            String url = moviePosterService.replacePosterFromTMDB(movieId);
            return ResponseEntity.ok(url);
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.notFound().build();
        } catch (Exception exception) {
            return ResponseEntity
                    .internalServerError()
                    .body("Ошибка получения постера: " + exception.getMessage());
        }
    }
}