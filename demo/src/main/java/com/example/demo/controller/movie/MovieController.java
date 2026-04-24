package com.example.demo.controller.movie;

import com.example.demo.controller.authorize.LogoutController;
import com.example.demo.dto.movie.MovieCardViewDTO;
import com.example.demo.service.MovieService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@RequestMapping("/movies")
public class MovieController {
    private final LogoutController logoutController;
    private final MovieService movieService;

    @GetMapping
    public String getList(
            @PageableDefault(size = 12,
                    sort = "releaseDate",
                    direction = Sort.Direction.DESC) Pageable pageable,
            Model model, HttpServletRequest httpServletRequest) {
        logoutController.logout(httpServletRequest);
        Page<MovieCardViewDTO> cards = movieService.getMovieCards(pageable);
        model.addAttribute("cardsPage", cards);
        return "movie/index";
    }

    @GetMapping("/{title}")
    public String showMovie(@PathVariable("title") String title,
                            Model model, HttpServletRequest httpServletRequest) {
        logoutController.logout(httpServletRequest);
        model.addAttribute("card", movieService.getCard(title));
        return "movie/show";
    }
}
