package com.example.demo.mapper.entity;

import com.example.demo.dto.catalog.RatedMovieForOwnerFormDTO;
import com.example.demo.dto.catalog.RatedMovieForOwnerViewDTO;
import com.example.demo.dto.catalog.ReviewViewDTO;
import com.example.demo.model.entity.RatedMovie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface RatedMapper {

    @Mapping(target = "reviewText", source = "review")
    @Mapping(target = "addedAt", source = "addedAt")
    @Mapping(target = "username", ignore = true)
    ReviewViewDTO toReviewView(RatedMovie ratedMovie);

    @Named("getUsername")
    default String getUsername(RatedMovie ratedMovie) {
        if (ratedMovie.getUser() == null || ratedMovie.getUser().isDeleted()) {
            return "Удалённый аккаунт";
        }
        return ratedMovie.getUser().getUsername();
    }

    default ReviewViewDTO toReviewViewWithOwn(RatedMovie ratedMovie, Long currentUserId) {
        ReviewViewDTO dto = toReviewView(ratedMovie);
        String username = getUsername(ratedMovie);
        boolean isOwn = ratedMovie.getUser().getId().equals(currentUserId);
        return ReviewViewDTO.builder()
                .username(username)
                .addedAt(dto.addedAt())
                .ratingValue(dto.ratingValue())
                .reviewText(dto.reviewText())
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