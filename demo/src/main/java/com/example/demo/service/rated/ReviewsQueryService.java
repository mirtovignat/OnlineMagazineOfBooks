package com.example.demo.service.rated;

import com.example.demo.dto.joined_to_user.ReviewViewDTO;
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
    public ReviewsPageResponse buildReviewsPage(Long movieId, String currentUsername) {
        List<ReviewViewDTO> reviews = ratedQueryService.getReviewsByMovieId(movieId, currentUsername);
        List<ReviewViewDTO> sortedReviews = Sorter.sortByReleaseDateConsideringOwn(reviews);
        BigDecimal avgRating = ratedQueryService.getMovieRating(movieId);
        long totalReviews = ratedQueryService.getReviewsCountForMovie(movieId);
        boolean hasOwnReview = currentUsername != null &&
                ratedCatalogService.existsByMovieIdAndUsername(movieId, currentUsername);
        String movieTitle = movieService.getMovie(movieId).getTitle();
        return new ReviewsPageResponse(
                sortedReviews,
                avgRating,
                totalReviews,
                hasOwnReview,
                movieTitle != null ? movieTitle : "Фильм",
                movieId != 0 ? movieId : 0
        );
    }
}