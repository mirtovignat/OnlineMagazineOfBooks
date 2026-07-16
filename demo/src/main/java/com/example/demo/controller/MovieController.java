package com.example.demo.controller;

import com.example.demo.dto.movie.MovieCardDetailsViewDTO;
import com.example.demo.dto.movie.MovieCardViewDTO;
import com.example.demo.dto.movie.MovieForUser;
import com.example.demo.dto.movie.MovieSearchDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.filter.MovieFilter;
import com.example.demo.model.Movie;
import com.example.demo.repository.MovieRepository;
import com.example.demo.service.MovieService;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@RequestMapping
public class MovieController {

    private final MovieService movieService;
    private final BadgeUpdater badgeUpdater;
    private final MovieRepository movieRepository;

    @GetMapping
    public String showMovies(
            @RequestParam(required = false) String title,
            @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO,
            @PageableDefault(size = 12, sort = "releaseDate", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        String username = prepareModelAndGetUsername(userForOwnerViewDTO, model);
        boolean isSearch = title != null && !title.isBlank();

        MovieSearchDTO searchDto = isSearch ? new MovieSearchDTO(null, title, null, null, null) : null;
        Page<MovieForUser<MovieCardViewDTO>> cardsPage = movieService.getMovieCards(username, searchDto, pageable);

        model.addAttribute("cardsPage", cardsPage);
        return "index";
    }

    @PostMapping("/search")
    public String searchMovies(
            @RequestBody List<MovieSearchDTO> foundMovies,
            @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO,
            @PageableDefault(size = 12, sort = "releaseDate", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        String username = prepareModelAndGetUsername(userForOwnerViewDTO, model);
        Page<MovieForUser<MovieCardViewDTO>> cardsPage = movieService.convertSearchDtoListToPage(foundMovies, pageable, username);

        model.addAttribute("cardsPage", cardsPage);
        model.addAttribute("currentSuggestions", foundMovies);
        return "index";
    }

    @GetMapping("/search-by-filters")
    public String searchMovies(MovieFilter movieFilter,
                               @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO,
                               @PageableDefault(size = 12, sort = "releaseDate", direction = Sort.Direction.DESC) Pageable pageable,
                               Model model) {

        String username = prepareModelAndGetUsername(userForOwnerViewDTO, model);
        Page<MovieForUser<MovieCardViewDTO>> cardsPage = movieService.convertMovieFilterToPage(movieFilter, pageable, username);

        model.addAttribute("cardsPage", cardsPage);
        model.addAttribute("movieFilter", movieFilter);
        return "index";
    }

    @GetMapping("/movies/{id}")
    public String showMovie(
            @PathVariable("id") Long id,
            @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO,
            Model model) {

        String username = prepareModelAndGetUsername(userForOwnerViewDTO, model);
        MovieForUser<MovieCardDetailsViewDTO> card = movieService.getCard(id, username);

        model.addAttribute("card", card);
        return "show";
    }

    @GetMapping("/rating/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMovieRating(@PathVariable Long id) {
        Movie movie = movieRepository.findFullByIdOrThrow(id);
        return ResponseEntity.ok(Map.of("rating", movie.getRating()));
    }

    @GetMapping("/search-suggestions")
    @ResponseBody
    public List<MovieSearchDTO> getSearchSuggestions(@RequestParam("movieSearchQuery") String movieSearchQuery) {
        return movieService.getMovieSearchDTO(movieSearchQuery);
    }

    @GetMapping("/directors")
    @ResponseBody
    public List<String> getAllDirectors() {
        return movieService.getAllDirectors();
    }

    @GetMapping("/genres")
    @ResponseBody
    public List<String> getAllGenres() {
        return movieService.getAllGenres();
    }

    private String prepareModelAndGetUsername(UserForOwnerViewDTO userForOwnerViewDTO, Model model) {
        if (userForOwnerViewDTO != null) {
            badgeUpdater.updateBadges(userForOwnerViewDTO, model);
            return userForOwnerViewDTO.username();
        }
        return null;
    }
}