package com.example.demo.mapper;

import com.example.demo.dto.joined_to_user.FavouriteMovieForOwnerViewDTO;
import com.example.demo.model.FavouriteMovie;
import com.example.demo.service.PresenceService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DurationMapper.class, MapperUtils.class})
public interface FavouritesMapper {

    @Mapping(target = "title", source = "movie.title")
    @Mapping(target = "price", source = "movie.price")
    @Mapping(target = "releaseDate", source = "movie.releaseDate")
    @Mapping(target = "formattedDuration", source = "movie.duration")
    @Mapping(target = "genre", source = "movie.genre")
    @Mapping(target = "rating", source = "movie.rating")
    @Mapping(target = "description", source = "movie.description")
    @Mapping(target = "userReviewWrittenAt", source = "addedAt")
    @Mapping(target = "inCart", expression =
            "java(presenceService.isInCart(favouriteMovie.getMovie().getTitle(), favouriteMovie.getUser().getUsername()))")
    @Mapping(target = "inFavourites", constant = "true")
    FavouriteMovieForOwnerViewDTO toOwnerView
            (FavouriteMovie favouriteMovie,
             @Context PresenceService presenceService);
}