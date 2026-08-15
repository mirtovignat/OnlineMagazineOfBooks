package com.example.demo.service.rated;

import com.example.demo.dto.joined_to_user.RatedMovieForOwnerFormDTO;
import com.example.demo.dto.joined_to_user.ReviewViewDTO;
import com.example.demo.mapper.RatedMapper;
import com.example.demo.model.RatedMovie;
import com.example.demo.repository.AbstractCatalogRepository;
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

    public List<ReviewViewDTO> getReviewsByMovieId(Long movieId, String currentUsername) {
        List<RatedMovie> ratedMovies = catalogRepository.findAllByMovieId(movieId);
        return ratedMovies.stream().map(ratedMovie -> {
            ReviewViewDTO reviewViewDTO = ratedMapper.toReviewView(ratedMovie, null);
            boolean isOwn = ratedMovie.getUser().getUsername().equals(currentUsername);
            return new ReviewViewDTO(
                    reviewViewDTO.username(),
                    reviewViewDTO.addedAt(),
                    reviewViewDTO.ratingValue(),
                    reviewViewDTO.reviewText(),
                    isOwn
            );
        }).toList();
    }

    public RatedMovieForOwnerFormDTO getPreFilledForm(Long movieId, String username) {
        return catalogRepository.findByMovieIdAndUserUsername(movieId, username)
                .map(ratedMapper::toFormView)
                .orElse(new RatedMovieForOwnerFormDTO(movieId, null, null));
    }

    public BigDecimal getMovieRating(Long movieId) {
        return movieService.getMovie(movieId).getRating();
    }

    public Long getReviewsCountForMovie(Long movieId) {
        return catalogRepository.countByMovieId(movieId);
    }

    public boolean isUnchanged(RatedMovieForOwnerFormDTO formDTO, RatedMovie ratedMovie) {
        if (formDTO == null || formDTO.rating() == null) {
            return false;
        }
        boolean isRatingSame = formDTO.rating().compareTo(ratedMovie.getRatingValue()) == 0;
        String newReview = formDTO.review() != null ? formDTO.review().trim() : "";
        String oldReview = ratedMovie.getReview() != null ? ratedMovie.getReview().trim() : "";
        boolean isReviewSame = newReview.equals(oldReview);
        return isRatingSame && isReviewSame;
    }
}