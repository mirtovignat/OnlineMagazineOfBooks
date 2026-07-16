package com.example.demo.mapper;

import com.example.demo.dto.movie.MovieCardDetailsViewDTO;
import com.example.demo.dto.movie.MovieCardViewDTO;
import com.example.demo.dto.movie.MovieSearchDTO;
import com.example.demo.model.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DurationMapper.class, MapperUtils.class})
public interface MovieMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "genre", source = "genre")
    @Mapping(target = "posterUrl", source = "posterUrl")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "releaseYear", source = "releaseDate", qualifiedByName = "yearFromLocalDate")
    @Mapping(target = "director", source = "director")
    @Mapping(target = "formattedDuration", source = "duration", qualifiedByName = "durationToString")
    @Mapping(target = "ratingsCount", source = "ratingsCount")
    MovieCardViewDTO toCard(Movie movie);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "genre", source = "genre")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "posterUrl", source = "posterUrl")
    @Mapping(target = "director", source = "director")
    @Mapping(target = "releaseDate", source = "releaseDate")
    @Mapping(target = "formattedDuration", source = "duration", qualifiedByName = "durationToString")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "ratingsCount", source = "ratingsCount")
    MovieCardDetailsViewDTO toDetails(Movie movie);

    @Mapping(target = "id", source = "movie.id")
    @Mapping(target = "title", source = "movie.title")
    @Mapping(target = "releaseYear", source = "releaseDate", qualifiedByName = "yearFromLocalDate")
    @Mapping(target = "genre", source = "movie.genre")
    @Mapping(target = "director", source = "movie.director")
    MovieSearchDTO toMovieSearch(Movie movie);
}