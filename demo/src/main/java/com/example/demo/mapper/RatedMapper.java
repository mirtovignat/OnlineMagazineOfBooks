package com.example.demo.mapper;

import com.example.demo.dto.joined_to_user.RatedMovieForOwnerFormDTO;
import com.example.demo.dto.joined_to_user.RatedMovieForOwnerViewDTO;
import com.example.demo.dto.joined_to_user.ReviewViewDTO;
import com.example.demo.model.RatedMovie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DurationMapper.class, MapperUtils.class})
public interface RatedMapper {

    @Mapping(target = "title", source = "movie.title")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "ratedAt", source = "ratedAt")
    @Mapping(target = "ratingValue", source = "ratingValue")
    @Mapping(target = "reviewText", source = "review")
    ReviewViewDTO toReviewView(RatedMovie ratedMovie);

    default ReviewViewDTO toReviewView(RatedMovie ratedMovie, String currentUsername) {
        ReviewViewDTO reviewViewDTO = toReviewView(ratedMovie);
        boolean own = currentUsername != null && currentUsername.equals(reviewViewDTO.username());
        return new ReviewViewDTO(
                reviewViewDTO.title(),
                reviewViewDTO.username(),
                reviewViewDTO.ratedAt(),
                reviewViewDTO.ratingValue(),
                reviewViewDTO.reviewText(),
                own
        );
    }

    @Mapping(target = "id", source = "movie.id")
    @Mapping(target = "title", source = "movie.title")
    @Mapping(target = "price", source = "movie.price")
    @Mapping(target = "genre", source = "movie.genre")
    @Mapping(target = "rating", source = "movie.rating")
    @Mapping(target = "ratedAt", source = "ratedAt")
    @Mapping(target = "ratingValue", source = "ratingValue")
    @Mapping(target = "reviewText", source = "review")
    @Mapping(target = "posterUrl", source = "movie.posterUrl")
    @Mapping(target = "releaseDate", source = "movie.releaseDate")
    @Mapping(target = "director", source = "movie.director")
    @Mapping(target = "formattedDuration", source = "movie.duration", qualifiedByName = "durationToString")
    RatedMovieForOwnerViewDTO toOwnerView(RatedMovie ratedMovie);

    @Mapping(target = "id", source = "movie.id")
    @Mapping(target = "review", source = "review")
    @Mapping(target = "rating", source = "ratingValue")
    RatedMovieForOwnerFormDTO toFormView(RatedMovie ratedMovie);
}