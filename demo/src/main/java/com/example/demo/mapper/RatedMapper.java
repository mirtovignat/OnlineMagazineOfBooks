package com.example.demo.mapper;

import com.example.demo.dto.joined_to_user.RatedMovieForOwnerViewDTO;
import com.example.demo.dto.joined_to_user.ReviewForUserViewDTO;
import com.example.demo.model.RatedMovie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DurationMapper.class, MapperUtils.class})
public interface RatedMapper {

    @Mapping(target = "title", source = "movie.title")
    @Mapping(target = "price", source = "movie.price")
    @Mapping(target = "releaseDate", source = "movie.releaseDate")
    @Mapping(target = "formattedDuration", source = "movie.duration")
    @Mapping(target = "genre", source = "movie.genre")
    @Mapping(target = "rating", source = "movie.rating")
    @Mapping(target = "description", source = "movie.description")
    @Mapping(target = "ratingsCount", source = "movie.ratingsCount", qualifiedByName = "nullableNumberToLong")
    @Mapping(target = "ratedAt", source = "ratedAt")
    @Mapping(target = "ratingValue", source = "ratingValue")
    @Mapping(target = "reviewText", source = "review")
    RatedMovieForOwnerViewDTO toOwnerView(RatedMovie ratedMovie);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "reviewText", source = "review")
    ReviewForUserViewDTO toReviewForUserView(RatedMovie ratedMovie);
}