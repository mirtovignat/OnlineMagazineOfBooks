package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.joined_to_user.RatedMovieForOwnerFormDTO;
import com.example.demo.dto.joined_to_user.RatedMovieForOwnerViewDTO;
import com.example.demo.dto.joined_to_user.ReviewViewDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.exception.SuccessCode;
import com.example.demo.service.RatedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/rated")
@RequiredArgsConstructor
public class RatedController {

    private final RatedService ratedService;
    private final BadgeUpdater badgeUpdater;

    @GetMapping("/{id}/reviews")
    public String getReviews(
            @PathVariable("id") Long movieId,
            @SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO,
            Model model,
            @PageableDefault(sort = "ratedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(defaultValue = "false") boolean edit) {

        String currentUsername = userForOwnerViewDTO != null ? userForOwnerViewDTO.username() : null;
        Page<ReviewViewDTO> reviewsPage = ratedService.getReviewsByMovieId(movieId, pageable, currentUsername);
        model.addAttribute("reviewsPage", reviewsPage);
        model.addAttribute("title", !reviewsPage.isEmpty() ? reviewsPage.getContent().get(0).title() : "Фильм");

        RatedMovieForOwnerFormDTO ratedMovieForOwnerFormDTO;
        boolean isRatedByCurrentUser = false;
        if (currentUsername != null) {
            ratedMovieForOwnerFormDTO = ratedService.getPreFilledForm(movieId, currentUsername);
            isRatedByCurrentUser = ratedMovieForOwnerFormDTO.rating() != null;
        } else {
            ratedMovieForOwnerFormDTO = new RatedMovieForOwnerFormDTO(movieId, null, null);
        }
        badgeUpdater.updateBadges(userForOwnerViewDTO, model);
        model.addAttribute("ratedForm", ratedMovieForOwnerFormDTO);
        model.addAttribute("editMode", edit);
        model.addAttribute("isRatedByCurrentUser", isRatedByCurrentUser);
        return "reviews";
    }

    @PostMapping("/{id}/reviews")
    public String addOrUpdateReview(
            @PathVariable("id") Long movieId,
            @Valid @ModelAttribute("ratedForm") RatedMovieForOwnerFormDTO form,
            BindingResult bindingResult,
            @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Неверный формат оценки или отзыва");
            return "redirect:/rated/" + movieId + "/reviews";
        }
        try {
            RatedMovieForOwnerFormDTO existingForm = ratedService.getPreFilledForm(movieId, userForOwnerViewDTO.username());
            boolean isUpdate = existingForm != null && existingForm.rating() != null;
            ratedService.addOrUpdateRating(movieId, userForOwnerViewDTO.username(), form);

            SuccessCode successCode = isUpdate
                    ? SuccessCode.REVIEW_HAS_BEEN_UPDATED_SUCCESSFULLY
                    : SuccessCode.REVIEW_HAS_BEEN_SAVED_SUCCESSFULLY;

            redirectAttributes.addFlashAttribute("successMessage", successCode.format(movieId));
        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.DATA_COINCIDENCE) {
                redirectAttributes.addFlashAttribute("infoMessage", e.getMessage());
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при сохранении отзыва");
        }
        return "redirect:/rated/" + movieId + "/reviews";
    }

    @PostMapping("/{id}/reviews/delete")
    public String deleteReview(
            @PathVariable("id") Long movieId,
            @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
            RedirectAttributes redirectAttributes) {

        try {
            ratedService.deleteRating(movieId, userForOwnerViewDTO.username());
            redirectAttributes.addFlashAttribute("successMessage",
                    SuccessCode.REVIEW_HAS_BEEN_DELETED_SUCCESSFULLY.format(movieId));
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении отзыва");
        }
        return "redirect:/rated/" + movieId + "/reviews";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<ApiResponse> addRating(
            @Valid @ModelAttribute RatedMovieForOwnerFormDTO ratedMovieForOwnerFormDTO,
            @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO) {

        try {
            ratedService.addOrUpdateRating(ratedMovieForOwnerFormDTO.id(),
                    userForOwnerViewDTO.username(), ratedMovieForOwnerFormDTO);

            BigDecimal newRating = ratedService.getMovieRating(ratedMovieForOwnerFormDTO.id());
            long reviewsCount = ratedService.getReviewsCountForMovie(ratedMovieForOwnerFormDTO.id());

            return ResponseEntity.ok(ApiResponse.success(
                    SuccessCode.REVIEW_HAS_BEEN_SAVED_SUCCESSFULLY,
                    Map.of(
                            "movieId", ratedMovieForOwnerFormDTO.id(),
                            "rating", newRating != null ? newRating : "-",
                            "reviewsCount", reviewsCount
                    )
            ));
        } catch (BusinessException businessException) {
            if (businessException.getErrorCode() == ErrorCode.DATA_COINCIDENCE) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        ApiResponse.error(businessException.getMessage()));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(businessException.getMessage()));
        } catch (Exception exception) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка при добавлении оценки"));
        }
    }

    @PostMapping("/edit")
    @ResponseBody
    public ResponseEntity<ApiResponse> editRating(
            @Valid @ModelAttribute RatedMovieForOwnerFormDTO ratedMovieForOwnerFormDTO,
            @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO) {

        try {
            ratedService.addOrUpdateRating(ratedMovieForOwnerFormDTO.id(), userForOwnerViewDTO.username(), ratedMovieForOwnerFormDTO);

            BigDecimal newRating = ratedService.getMovieRating(ratedMovieForOwnerFormDTO.id());
            long reviewsCount = ratedService.getReviewsCountForMovie(ratedMovieForOwnerFormDTO.id());

            return ResponseEntity.ok(ApiResponse.success(
                    SuccessCode.REVIEW_HAS_BEEN_UPDATED_SUCCESSFULLY,
                    Map.of(
                            "movieId", ratedMovieForOwnerFormDTO.id(),
                            "rating", newRating != null ? newRating : "-",
                            "reviewsCount", reviewsCount
                    )
            ));
        } catch (BusinessException businessException) {
            if (businessException.getErrorCode() == ErrorCode.DATA_COINCIDENCE) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(businessException.getMessage()));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(businessException.getMessage()));
        } catch (Exception exception) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка при обновлении оценки"));
        }
    }

    @PostMapping("/remove/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse> removeRating(
            @PathVariable("id") Long movieId,
            @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO) {

        try {
            ratedService.deleteRating(movieId, userForOwnerViewDTO.username());

            BigDecimal newRating = ratedService.getMovieRating(movieId);
            long reviewsCount = ratedService.getReviewsCountForMovie(movieId);

            return ResponseEntity.ok(ApiResponse.success(
                    SuccessCode.REVIEW_HAS_BEEN_DELETED_SUCCESSFULLY,
                    Map.of(
                            "movieId", movieId,
                            "rating", newRating != null ? newRating : "-",
                            "reviewsCount", reviewsCount
                    )
            ));
        } catch (Exception exception) {
            return ResponseEntity.badRequest().body(ApiResponse.error(exception.getMessage()));
        }
    }

    @GetMapping("/rating/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMovieRating(@PathVariable("id") Long movieId) {
        BigDecimal rating = ratedService.getMovieRating(movieId);
        return ResponseEntity.ok(Map.of("rating", rating != null ? rating : "-"));
    }

    @GetMapping("/history")
    public String getRatedHistory(
            @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
            Model model,
            @PageableDefault(size = 12, sort = "ratedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<RatedMovieForOwnerViewDTO> ratedHistory = ratedService.getRatedHistory(userForOwnerViewDTO.username(), pageable);
        model.addAttribute("ratedHistory", ratedHistory);
        badgeUpdater.updateBadges(userForOwnerViewDTO, model);
        return "rated-history";
    }

    @GetMapping("/count/{movieId}")
    @ResponseBody
    public ResponseEntity<Long> getReviewsCount(@PathVariable Long movieId) {
        return ResponseEntity.ok(ratedService.getReviewsCountForMovie(movieId));
    }
}