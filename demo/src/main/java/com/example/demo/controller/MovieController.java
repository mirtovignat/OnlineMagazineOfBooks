package com.example.demo.controller;

import com.example.demo.dto.movie.MovieCardDetailsViewDTO;
import com.example.demo.dto.movie.MovieCardViewDTO;
import com.example.demo.dto.movie.MovieForUser;
import com.example.demo.dto.movie.MovieSearchDTO;
import com.example.demo.dto.response.MoviesPageResponse;
import com.example.demo.dto.user.SessionUser;
import com.example.demo.filter.MovieFilter;
import com.example.demo.service.movie.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public String showCatalog(
            @SessionAttribute(required = false) SessionUser sessionUser,
            @PageableDefault(size = 15, sort = "releaseDate", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        Long userId = sessionUser != null ? sessionUser.id() : null;
        Page<MovieForUser<MovieCardViewDTO>> cardsPage =
                movieService.getCatalogPage(pageable, userId);
        MoviesPageResponse response = MoviesPageResponse.builder()
                .cardsPage(cardsPage)
                .filter(null)
                .isSearch(false)
                .build();
        model.addAttribute("response", response);
        return "index";
    }

    @GetMapping("/search")
    public String searchMovies(
            @Valid @ModelAttribute MovieFilter movieFilter,
            @PageableDefault(size = 15, sort = "releaseDate",
                    direction = Sort.Direction.DESC) Pageable pageable,
            @SessionAttribute(required = false) SessionUser sessionUser,
            Model model
    ) {
        Long userId = sessionUser != null ? sessionUser.id() : null;
        Page<MovieForUser<MovieCardViewDTO>> cardsPage = movieService.getMoviesByFilter(
                movieFilter, pageable, userId);
        if (cardsPage.getTotalElements() == 1) {
            Long movieId = cardsPage.getContent().get(0).movie().id();
            return "redirect:/movies/" + movieId;
        }
        MoviesPageResponse response = MoviesPageResponse.builder()
                .cardsPage(cardsPage)
                .filter(movieFilter)
                .isSearch(true)
                .build();
        model.addAttribute("response", response);
        return "index";
    }

    @GetMapping("/movies/{id}")
    public String showMovie(
            @PathVariable Long id,
            @SessionAttribute(required = false) SessionUser sessionUser,
            Model model
    ) {
        Long userId = sessionUser != null ? sessionUser.id() : null;
        MovieForUser<MovieCardDetailsViewDTO> card = movieService.getCard(id, userId);
        model.addAttribute("card", card);
        return "show";
    }

    @GetMapping("/movies/{id}/rating")
    @ResponseBody
    public ResponseEntity<Map<String, Double>> getMovieRating(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("rating", movieService.getMovieRating(id)));
    }

    @GetMapping("/search-suggestions")
    @ResponseBody
    public ResponseEntity<List<MovieSearchDTO>> getSearchSuggestions(@RequestParam String query) {
        return ResponseEntity.ok(movieService.getMovieSearchDTO(query));
    }

    @GetMapping("/directors")
    @ResponseBody
    public ResponseEntity<List<String>> getAllDirectors() {
        return ResponseEntity.ok(movieService.getAllDirectors());
    }

    @GetMapping("/genres")
    @ResponseBody
    public ResponseEntity<List<String>> getAllGenres() {
        return ResponseEntity.ok(movieService.getAllGenres());
    }
}