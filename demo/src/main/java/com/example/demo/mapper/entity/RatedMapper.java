package com.example.demo.mapper.entity;

import com.example.demo.dto.catalog.RatedMovieForOwnerFormDTO;
import com.example.demo.dto.catalog.RatedMovieForOwnerViewDTO;
import com.example.demo.dto.catalog.ReviewViewDTO;
import com.example.demo.model.entity.RatedMovie;
import com.example.demo.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RatedMapper {

    @Mapping(target = "reviewText", source = "review")
    @Mapping(target = "addedAt", source = "addedAt")
    @Mapping(target = "username", ignore = true)
    ReviewViewDTO toReviewView(RatedMovie ratedMovie);

    default String getUsername(RatedMovie ratedMovie) {
        User user = ratedMovie.getUser();
        if (user == null || user.isDeleted()) {
            return "Удалённый аккаунт";
        }
        return user.getUsername();
    }

    default ReviewViewDTO toReviewViewWithOwn(RatedMovie ratedMovie, Long currentUserId) {
        ReviewViewDTO reviewViewDTO = toReviewView(ratedMovie);
        String username = getUsername(ratedMovie);
        boolean isOwn = ratedMovie.getUser() != null && ratedMovie.getUser().getId().equals(currentUserId);
        return ReviewViewDTO.builder()
                .username(username)
                .addedAt(reviewViewDTO.addedAt())
                .ratingValue(reviewViewDTO.ratingValue())
                .reviewText(reviewViewDTO.reviewText())
                .own(isOwn)
                .build();
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

    @Mapping(target = "id", source = "movie.id")
    @Mapping(target = "rating", source = "ratingValue")
    @Mapping(target = "review", source = "review")
    RatedMovieForOwnerFormDTO toFormView(RatedMovie ratedMovie);
}