package com.example.demo.controller.rated;

import com.example.demo.controller.authorize.LogoutController;
import com.example.demo.service.RatedService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@AllArgsConstructor
@Controller
@RequestMapping("/rated")
public class RatedController {

    @Autowired
    private final LogoutController logoutController;

    @Autowired
    private final RatedService ratedService;

    @GetMapping("/{title}/reviews")
    public String getMovieReviews(Model model,
                                  @PathVariable("title")
                                  String title,
                                  HttpServletRequest httpServletRequest) {
        logoutController.invalidate(httpServletRequest);
        model.addAttribute("reviews",
                ratedService.getMovieReviewsForUser(title));
        return "movie/reviews";
    }
}
