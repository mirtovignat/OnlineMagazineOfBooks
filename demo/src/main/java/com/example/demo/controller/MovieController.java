package com.example.demo.controller;

import com.example.demo.dto.movie.MovieCardDetailsViewDTO;
import com.example.demo.dto.movie.MovieCardViewDTO;
import com.example.demo.dto.movie.MovieForUser;
import com.example.demo.dto.movie.MovieSearchDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.filter.MovieFilter;
import com.example.demo.service.movie.MovieService;
import com.example.demo.service.user.UserService;
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
    private final UserService userService;

    @GetMapping
    public String showCatalog(
            @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO,
            @PageableDefault(size = 15, sort = "releaseDate", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        String username = userService.getUsername(userForOwnerViewDTO);
        Page<MovieForUser<MovieCardViewDTO>> cardsPage = movieService.getCatalogPage(pageable, username);
        model.addAttribute("cardsPage", cardsPage);
        model.addAttribute("isSearch", false);
        return "index";
    }

    @GetMapping("/search")
    public String searchMovies(
            @Valid @ModelAttribute MovieFilter movieFilter,
            @PageableDefault(size = 15, sort = "releaseDate", direction = Sort.Direction.DESC) Pageable pageable,
            @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO,
            Model model
    ) {
        String username = userService.getUsername(userForOwnerViewDTO);
        Page<MovieForUser<MovieCardViewDTO>> cardsPage = movieService.getMoviesByFilter(movieFilter, pageable, username);
        model.addAttribute("cardsPage", cardsPage);
        model.addAttribute("movieFilter", movieFilter);
        model.addAttribute("isSearch", true);
        return "index";
    }

    @GetMapping("/movies/{id}")
    public String showMovie(
            @PathVariable Long id,
            @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO,
            Model model
    ) {
        String username = userService.getUsername(userForOwnerViewDTO);
        MovieForUser<MovieCardDetailsViewDTO> card = movieService.getCard(id, username);
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