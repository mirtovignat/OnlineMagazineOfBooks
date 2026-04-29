package com.example.demo.controller.rated;

import com.example.demo.dto.joined_to_user.RatedMovieForOwnerFormDTO;
import com.example.demo.dto.joined_to_user.ReviewForUserViewDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.user.DataCoincidenceException;
import com.example.demo.model.Movie;
import com.example.demo.service.MovieService;
import com.example.demo.service.RatedService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Controller
@RequestMapping("/user")
public class RatedForUserController {

    private final RatedService ratedService;
    private final MovieService movieService;

    @GetMapping("/rated/{title}/reviews/json")
    @ResponseBody
    public ResponseEntity<List<ReviewForUserViewDTO>> getMovieReviewsJson(
            @PathVariable("title") String title) {
        return ResponseEntity.ok(ratedService.getMovieReviewsForUser(title));
    }


    @GetMapping("/rated/{title}/reviews")
    public String getMovieReviews(Model model,
                                  @PathVariable("title") String title,
                                  @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO) {
        model.addAttribute("currentUsername", userForOwnerViewDTO.username());
        model.addAttribute("isRatedByCurrentUser", ratedService.isRatedByUser(title, userForOwnerViewDTO.username()));
        model.addAttribute("reviews", ratedService.getMovieReviewsForUser(title));
        model.addAttribute("title", title);
        return "for_user/movie/reviews";
    }

    @GetMapping("/movies/rating/{title}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMovieRating(
            @PathVariable("title") String title) {
        Movie movie = movieService.findByTitleOrThrow(title);
        BigDecimal rating = movie.getRating();
        return ResponseEntity.ok(Map.of("rating", rating != null ? rating : "-"));
    }

    @PostMapping("/rated/add")
    @ResponseBody
    public ResponseEntity<Map<String, String>> rate(@Valid @ModelAttribute RatedMovieForOwnerFormDTO ratedMovieForOwnerFormDTO,
                                                    @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO) {
        if (userForOwnerViewDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Не авторизован"));
        }
        try {
            ratedService.rate(ratedMovieForOwnerFormDTO, userForOwnerViewDTO.username());
            return ResponseEntity.ok(Map.of("message", "Оценка сохранена"));
        } catch (DataCoincidenceException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Данные не изменены"));
        }
    }

    @PostMapping("/rated/edit")
    @ResponseBody
    public ResponseEntity<Map<String, String>> editRating(@Valid @ModelAttribute RatedMovieForOwnerFormDTO ratedMovieForOwnerFormDTO,
                                                          @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO) {
        if (userForOwnerViewDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Не авторизован"));
        }
        try {
            ratedService.updateRating(ratedMovieForOwnerFormDTO, userForOwnerViewDTO.username());
            return ResponseEntity.ok(Map.of("message", "Оценка обновлена"));
        } catch (DataCoincidenceException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Данные не изменены"));
        }
    }

    @PostMapping("/rated/remove/{title}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> removeRating(@PathVariable("title") String title,
                                                            @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO) {
        if (userForOwnerViewDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Не авторизован"));
        }

        ratedService.removeRating(title, userForOwnerViewDTO.username());
        return ResponseEntity.ok(Map.of("message", "Оценка удалена"));

    }

}