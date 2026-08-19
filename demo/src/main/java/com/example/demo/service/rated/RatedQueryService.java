package com.example.demo.service.rated;

import com.example.demo.dto.catalog.RatedMovieForOwnerFormDTO;
import com.example.demo.dto.catalog.ReviewViewDTO;
import com.example.demo.mapper.entity.RatedMapper;
import com.example.demo.model.entity.RatedMovie;
import com.example.demo.repository.base.AbstractCatalogRepository;
import com.example.demo.service.movie.MovieService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@Service
@Transactional(readOnly = true)
public class RatedQueryService {

    private final AbstractCatalogRepository<RatedMovie> catalogRepository;
    private final RatedMapper ratedMapper;
    private final MovieService movieService;

    public List<ReviewViewDTO> getReviewsByMovieId(Long movieId, Long currentUserId) {
        List<RatedMovie> ratedMovies = catalogRepository.findAllByMovieId(movieId);
        return ratedMovies.stream()
                .map(ratedMovie -> ratedMapper.toReviewViewWithOwn(ratedMovie, currentUserId))
                .toList();
    }

    public RatedMovieForOwnerFormDTO getPreFilledForm(Long movieId, Long userId) {
        return catalogRepository.findByMovieIdAndUserId(movieId, userId)
                .map(ratedMapper::toFormView)
                .orElse(new RatedMovieForOwnerFormDTO(movieId, null, null));
    }

    public BigDecimal getMovieRating(Long movieId) {
        return movieService.getMovie(movieId).getRating();
    }

    public Long getReviewsCountForMovie(Long movieId) {
        return catalogRepository.countByMovieId(movieId);
    }

    public boolean isUnchanged(RatedMovieForOwnerFormDTO ratedMovieForOwnerFormDTO,
                               RatedMovie ratedMovie) {
        if (ratedMovieForOwnerFormDTO == null || ratedMovieForOwnerFormDTO.rating() == null) {
            return false;
        }

        if (ratedMovie.getRatingValue() == null) {
            return false;
        }

        boolean isRatingSame = ratedMovieForOwnerFormDTO.rating().compareTo(ratedMovie.getRatingValue()) == 0;
        String newReview = ratedMovieForOwnerFormDTO.review() != null ? ratedMovieForOwnerFormDTO
                .review().trim() : "";
        String oldReview = ratedMovie.getReview() != null ? ratedMovie.getReview().trim() : "";
        boolean isReviewSame = newReview.equals(oldReview);
        return isRatingSame && isReviewSame;
    }
}