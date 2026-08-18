package com.example.demo.service.rated;

import com.example.demo.dto.catalog.ReviewViewDTO;
import com.example.demo.dto.response.ReviewsPageResponse;
import com.example.demo.service.catalog.RatedCatalogService;
import com.example.demo.service.movie.MovieService;
import com.example.demo.sorter.Sorter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewsQueryService {

    private final RatedQueryService ratedQueryService;
    private final RatedCatalogService ratedCatalogService;
    private final MovieService movieService;

    @Transactional(readOnly = true)
    public ReviewsPageResponse buildReviewsPage(Long movieId, Long userId) {
        List<ReviewViewDTO> reviews = ratedQueryService.getReviewsByMovieId(movieId, userId);
        List<ReviewViewDTO> sortedReviews = Sorter.sortByReleaseDateConsideringOwn(reviews);
        BigDecimal averageRating = ratedQueryService.getMovieRating(movieId);
        long totalReviews = ratedQueryService.getReviewsCountForMovie(movieId);
        boolean hasOwnReview = userId != null &&
                ratedCatalogService.existsByMovieIdAndUserId(movieId, userId);
        String movieTitle = movieService.getMovie(movieId).getTitle();
        return ReviewsPageResponse.of(
                sortedReviews,
                averageRating,
                totalReviews,
                hasOwnReview,
                movieTitle,
                movieId
        );
    }
}