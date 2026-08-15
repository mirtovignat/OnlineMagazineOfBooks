package com.example.demo.mapper;

import com.example.demo.dto.joined_to_user.FavouriteMovieForOwnerViewDTO;
import com.example.demo.model.FavouriteMovie;
import com.example.demo.service.precence.FavouritePresenceService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DurationMapper.class, MapperUtils.class})
public interface FavouritesMapper {

    @Mapping(target = "id", source = "movie.id")
    @Mapping(target = "title", source = "movie.title")
    @Mapping(target = "genre", source = "movie.genre")
    @Mapping(target = "posterUrl", source = "movie.posterUrl")
    @Mapping(target = "rating", source = "movie.rating")
    @Mapping(target = "director", source = "movie.director")
    @Mapping(target = "price", source = "movie.price")
    @Mapping(target = "releaseYear", source = "movie.releaseDate", qualifiedByName = "yearFromLocalDate")
    @Mapping(target = "formattedDuration", source = "movie.duration", qualifiedByName = "durationToString")
    @Mapping(target = "inFavourites", constant = "true")
    @Mapping(target = "inCart", expression =
            "java(favouritePresenceService.isInLinkedCollection(favouriteMovie.getMovie().getId(), favouriteMovie.getUser().getUsername()))")
    FavouriteMovieForOwnerViewDTO toOwnerView(
            FavouriteMovie favouriteMovie,
            @Context FavouritePresenceService favouritePresenceService
    );
}