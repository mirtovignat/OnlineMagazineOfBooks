package com.example.demo.mapper;

import com.example.demo.dto.catalog.HistoricalMovieForOwnerViewDTO;
import com.example.demo.dto.catalog.LibrarianMovieForOwnerViewDTO;
import com.example.demo.model.PurchasedMovie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PurchasedMapper {

    @Mapping(target = "id", source = "movie.id")
    @Mapping(target = "title", source = "movie.title")
    @Mapping(target = "genre", source = "movie.genre")
    @Mapping(target = "posterUrl", source = "movie.posterUrl")
    @Mapping(target = "rating", source = "movie.rating")
    @Mapping(target = "releaseDate", source = "movie.releaseDate")
    @Mapping(target = "director", source = "movie.director")
    LibrarianMovieForOwnerViewDTO toOwnerViewFromLibrarian(PurchasedMovie purchasedMovie);

    @Mapping(target = "id", source = "movie.id")
    @Mapping(target = "title", source = "movie.title")
    @Mapping(target = "priceSnapshot", source = "priceSnapshot")
    @Mapping(target = "addedAt", source = "addedAt")
    HistoricalMovieForOwnerViewDTO toOwnerViewFromHistorical(PurchasedMovie purchasedMovie);
}