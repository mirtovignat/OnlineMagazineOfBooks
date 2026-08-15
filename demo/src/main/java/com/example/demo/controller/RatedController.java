package com.example.demo.controller;

import com.example.demo.dto.joined_to_user.RatedMovieForOwnerFormDTO;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.ReviewsPageResponse;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.SuccessCode;
import com.example.demo.service.rated.RatedCommandService;
import com.example.demo.service.rated.RatedQueryService;
import com.example.demo.service.rated.ReviewsQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/rated")
@RequiredArgsConstructor
public class RatedController {

    private final RatedCommandService ratedCommandService;
    private final RatedQueryService ratedQueryService;
    private final ReviewsQueryService reviewsPageService;

    @GetMapping("/{id}/reviews")
    public String getReviews(
            @PathVariable Long id,
            @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO,
            Model model
    ) {
        String currentUsername = userForOwnerViewDTO != null ? userForOwnerViewDTO.username() : null;
        ReviewsPageResponse response = reviewsPageService.buildReviewsPage(id, currentUsername);
        model.addAttribute("response", response);
        return "reviews";
    }

    @PostMapping("/{id}/reviews/delete")
    public String deleteReview(@PathVariable("id") Long movieId,
                               @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
                               RedirectAttributes redirectAttributes) {
        ratedCommandService.deleteRating(movieId, userForOwnerViewDTO.username());
        redirectAttributes.addFlashAttribute("successMessage",
                SuccessCode.REVIEW_HAS_BEEN_DELETED_SUCCESSFULLY.format(movieId));
        return "redirect:/rated/" + movieId + "/reviews";
    }

    @GetMapping("/form/{movieId}")
    @ResponseBody
    public ResponseEntity<RatedMovieForOwnerFormDTO> getMyReviewForm(
            @PathVariable Long movieId,
            @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO
    ) {
        return ResponseEntity.ok(ratedQueryService.getPreFilledForm(movieId, userForOwnerViewDTO.username()));
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<ApiResponse> addRating(@Valid @ModelAttribute RatedMovieForOwnerFormDTO ratedMovieForOwnerFormDTO,
                                                 @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO) {
        ratedCommandService.addOrUpdateRating(ratedMovieForOwnerFormDTO.id(),
                userForOwnerViewDTO.username(), ratedMovieForOwnerFormDTO);

        BigDecimal newRating = ratedQueryService.getMovieRating(ratedMovieForOwnerFormDTO.id());
        long reviewsCount = ratedQueryService.getReviewsCountForMovie(ratedMovieForOwnerFormDTO.id());

        return ResponseEntity.ok(ApiResponse.success(
                SuccessCode.REVIEW_HAS_BEEN_SAVED_SUCCESSFULLY,
                Map.of(
                        "movieId", ratedMovieForOwnerFormDTO.id(),
                        "rating", newRating != null ? newRating : "-",
                        "reviewsCount", reviewsCount
                )
        ));
    }

    @PostMapping("/edit")
    @ResponseBody
    public ResponseEntity<ApiResponse> editRating(@Valid @ModelAttribute RatedMovieForOwnerFormDTO ratedMovieForOwnerFormDTO,
                                                  @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO) {
        ratedCommandService.addOrUpdateRating(ratedMovieForOwnerFormDTO.id(), userForOwnerViewDTO.username(),
                ratedMovieForOwnerFormDTO);
        BigDecimal newRating = ratedQueryService.getMovieRating(ratedMovieForOwnerFormDTO.id());
        long reviewsCount = ratedQueryService.getReviewsCountForMovie(ratedMovieForOwnerFormDTO.id());
        return ResponseEntity.ok(ApiResponse.success(
                SuccessCode.REVIEW_HAS_BEEN_UPDATED_SUCCESSFULLY,
                Map.of(
                        "movieId", ratedMovieForOwnerFormDTO.id(),
                        "rating", newRating != null ? newRating : "-",
                        "reviewsCount", reviewsCount
                )
        ));
    }

    @PostMapping("/remove/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse> removeRating(@PathVariable("id") Long movieId,
                                                    @SessionAttribute("userForOwnerViewDTO") UserForOwnerViewDTO userForOwnerViewDTO) {
        ratedCommandService.deleteRating(movieId, userForOwnerViewDTO.username());
        BigDecimal newRating = ratedQueryService.getMovieRating(movieId);
        long reviewsCount = ratedQueryService.getReviewsCountForMovie(movieId);
        return ResponseEntity.ok(ApiResponse.success(
                SuccessCode.REVIEW_HAS_BEEN_DELETED_SUCCESSFULLY,
                Map.of(
                        "movieId", movieId,
                        "rating", newRating != null ? newRating : "-",
                        "reviewsCount", reviewsCount
                )
        ));
    }

    @GetMapping("/rating/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMovieRating(@PathVariable("id") Long movieId) {
        BigDecimal rating = ratedQueryService.getMovieRating(movieId);
        return ResponseEntity.ok(Map.of("rating", rating != null ? rating : "-"));
    }

    @GetMapping("/count/{movieId}")
    @ResponseBody
    public ResponseEntity<Long> getReviewsCount(@PathVariable Long movieId) {
        return ResponseEntity.ok(ratedQueryService.getReviewsCountForMovie(movieId));
    }
}