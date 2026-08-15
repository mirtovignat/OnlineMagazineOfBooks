package com.example.demo.mapper;

import com.example.demo.dto.movie.MovieCardDetailsViewDTO;
import com.example.demo.dto.movie.MovieCardViewDTO;
import com.example.demo.dto.movie.MovieSearchDTO;
import com.example.demo.model.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DurationMapper.class, MapperUtils.class})
public interface MovieMapper {

    @Mapping(target = "releaseYear", source = "releaseDate", qualifiedByName = "yearFromLocalDate")
    @Mapping(target = "formattedDuration", source = "duration", qualifiedByName = "durationToString")
    MovieCardViewDTO toCard(Movie movie);

    @Mapping(target = "formattedDuration", source = "duration", qualifiedByName = "durationToString")
    MovieCardDetailsViewDTO toDetails(Movie movie);

    @Mapping(target = "releaseYear",
            source = "releaseDate",
            qualifiedByName = "yearFromLocalDate")
    MovieSearchDTO toMovieSearch(Movie movie);
}