package com.example.demo.mapper;

import com.example.demo.dto.joined_to_user.CartMovieForOwnerViewDTO;
import com.example.demo.model.CartItem;
import com.example.demo.service.PresenceService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MapperUtils.class, DurationMapper.class})
public interface CartItemMapper {

    @Mapping(target = "id", source = "movie.id")
    @Mapping(target = "title", source = "movie.title")
    @Mapping(target = "unitPriceSnapshot", source = "unitPriceSnapshot")
    @Mapping(target = "addedAt", source = "addedAt")
    @Mapping(target = "inCart", constant = "true")
    @Mapping(target = "inFavourites", expression =
            "java(presenceService.isInFavourites(cartItem.getMovie().getTitle(), cartItem.getUser().getUsername()))")
    @Mapping(target = "genre", source = "movie.genre")
    @Mapping(target = "posterUrl", source = "movie.posterUrl")
    @Mapping(target = "rating", source = "movie.rating")
    @Mapping(target = "releaseYear", source = "movie.releaseDate", qualifiedByName = "yearFromLocalDate")
    @Mapping(target = "director", source = "movie.director")
    @Mapping(target = "formattedDuration", source = "movie.duration", qualifiedByName = "durationToString")
    CartMovieForOwnerViewDTO toOwnerView(CartItem cartItem,
                                         @Context PresenceService presenceService);
}