package com.example.demo.mapper;

import com.example.demo.dto.joined_to_user.RatedMovieForOwnerFormDTO;
import com.example.demo.dto.joined_to_user.RatedMovieForOwnerViewDTO;
import com.example.demo.dto.joined_to_user.ReviewViewDTO;
import com.example.demo.model.RatedMovie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RatedMapper {

    @Mapping(target = "reviewText", source = "review")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "addedAt", source = "addedAt")
    ReviewViewDTO toReviewView(RatedMovie ratedMovie);

    default ReviewViewDTO toReviewView(RatedMovie ratedMovie, String currentUsername) {
        ReviewViewDTO reviewViewDTO = toReviewView(ratedMovie);
        boolean own = currentUsername != null && currentUsername.equals(reviewViewDTO.username());
        return new ReviewViewDTO(
                reviewViewDTO.username(),
                reviewViewDTO.addedAt(),
                reviewViewDTO.ratingValue(),
                reviewViewDTO.reviewText(),
                own
        );
    }

    @Mapping(target = "id", source = "movie.id")
    @Mapping(target = "title", source = "movie.title")
    @Mapping(target = "genre", source = "movie.genre")
    @Mapping(target = "posterUrl", source = "movie.posterUrl")
    @Mapping(target = "rating", source = "movie.rating")
    @Mapping(target = "releaseDate", source = "movie.releaseDate")
    @Mapping(target = "review", source = "review")
    @Mapping(target = "addedAt", source = "addedAt")
    @Mapping(target = "ratingValue", source = "ratingValue")
    RatedMovieForOwnerViewDTO toOwnerView(RatedMovie ratedMovie);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "rating", source = "ratingValue")
    @Mapping(target = "review", source = "review")
    RatedMovieForOwnerFormDTO toFormView(RatedMovie ratedMovie);
}