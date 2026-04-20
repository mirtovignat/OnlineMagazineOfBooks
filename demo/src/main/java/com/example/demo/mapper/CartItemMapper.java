package com.example.demo.mapper;

import com.example.demo.dto.joined_to_user.CartMovieForOwnerViewDTO;
import com.example.demo.model.CartItem;
import com.example.demo.service.PresenceService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "title", source = "movie.title")
    @Mapping(target = "unitPriceSnapshot", source = "unitPriceSnapshot")
    @Mapping(target = "addedAt", source = "addedAt")
    @Mapping(target = "inCart", constant = "true")
    @Mapping(target = "inFavourites", expression =
            "java(presenceService.isInFavourites(cartItem.getMovie().getTitle(), cartItem.getUser().getUsername()))")
    CartMovieForOwnerViewDTO toOwnerView(CartItem cartItem,
                                         @Context PresenceService presenceService);
}