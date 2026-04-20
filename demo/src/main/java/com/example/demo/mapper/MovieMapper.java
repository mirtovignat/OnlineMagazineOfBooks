package com.example.demo.mapper;

import com.example.demo.dto.movie.MovieCardDetailsForUserViewDTO;
import com.example.demo.dto.movie.MovieCardDetailsViewDTO;
import com.example.demo.dto.movie.MovieCardForUserViewDTO;
import com.example.demo.dto.movie.MovieCardViewDTO;
import com.example.demo.model.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DurationMapper.class, MapperUtils.class})
public interface MovieMapper {
    @Mapping(target = "formattedDuration", source = "duration")
    @Mapping(target = "releaseYear", source = "releaseDate", qualifiedByName = "yearFromLocalDate")
    MovieCardViewDTO toCard(Movie movie);

    @Mapping(target = "releaseYear", source = "releaseDate", qualifiedByName = "yearFromLocalDate")
    @Mapping(target = "formattedDuration", source = "duration")
    @Mapping(target = "purchasesCount", source = "purchases", qualifiedByName = "sizeToLong")
    @Mapping(target = "ratingsCount", source = "ratingsCount", qualifiedByName = "nullableNumberToLong")
    @Mapping(target = "director", source = "director")
    MovieCardDetailsViewDTO toDetails(Movie movie);

    @Mapping(target = "formattedDuration", source = "duration")
    @Mapping(target = "releaseYear", source = "releaseDate", qualifiedByName = "yearFromLocalDate")
    @Mapping(target = "bought", constant = "false")
    @Mapping(target = "inCart", constant = "false")
    @Mapping(target = "inFavourites", constant = "false")
    MovieCardForUserViewDTO toCardForUser(Movie movie);

    @Mapping(target = "releaseYear", source = "releaseDate", qualifiedByName = "yearFromLocalDate")
    @Mapping(target = "formattedDuration", source = "duration")
    @Mapping(target = "purchasesCount", source = "purchases", qualifiedByName = "sizeToLong")
    @Mapping(target = "ratingsCount", source = "ratingsCount", qualifiedByName = "nullableNumberToLong")
    @Mapping(target = "bought", ignore = true)
    @Mapping(target = "inCart", ignore = true)
    @Mapping(target = "inFavourites", ignore = true)
    @Mapping(target = "director", source = "director")
    MovieCardDetailsForUserViewDTO toDetailsForUser(Movie movie);

    default MovieCardDetailsForUserViewDTO toDetailsForUserWithStatus(
            Movie movie,
            boolean bought,
            boolean inCart,
            boolean inFavourites
    ) {
        MovieCardDetailsForUserViewDTO dto = toDetailsForUser(movie);
        return new MovieCardDetailsForUserViewDTO(
                dto.title(),
                dto.price(),
                dto.releaseDate(),
                dto.releaseYear(),
                dto.formattedDuration(),
                dto.genre(),
                dto.rating(),
                dto.description(),
                dto.director(),
                dto.purchasesCount(),
                dto.ratingsCount(),
                bought,
                inCart,
                inFavourites
        );
    }

    default MovieCardForUserViewDTO toCardForUserWithStatus(
            Movie movie,
            boolean bought,
            boolean inCart,
            boolean inFavourites
    ) {
        MovieCardForUserViewDTO dto = toCardForUser(movie);
        return new MovieCardForUserViewDTO(
                dto.title(),
                dto.price(),
                dto.releaseYear(),
                dto.formattedDuration(),
                dto.genre(),
                dto.rating(),
                dto.director(),
                bought, !bought && inCart, inFavourites
        );
    }
}