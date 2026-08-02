package com.example.demo.mapper;

import com.example.demo.dto.joined_to_user.HistoricalMovieForOwnerViewDTO;
import com.example.demo.dto.joined_to_user.LibrarianMovieForOwnerViewDTO;
import com.example.demo.model.PurchasedMovie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MapperUtils.class, DurationMapper.class})
public interface PurchasedMapper {

    @Mapping(target = "id", source = "movie.id")
    @Mapping(target = "title", source = "movie.title")
    @Mapping(target = "price", source = "movie.price")
    @Mapping(target = "releaseDate", source = "movie.releaseDate")
    @Mapping(target = "formattedDuration", source = "movie.duration",
            qualifiedByName = "durationToString")
    @Mapping(target = "genre", source = "movie.genre")
    @Mapping(target = "posterUrl", source = "movie.posterUrl")
    @Mapping(target = "rating", source = "movie.rating")
    @Mapping(target = "description", source = "movie.description")
    @Mapping(target = "purchasesCount", source = "movie.purchases", qualifiedByName = "sizeToLong")
    @Mapping(target = "director", source = "movie.director")
    @Mapping(target = "ratingsCount", source = "movie.ratingsCount")
    LibrarianMovieForOwnerViewDTO toOwnerViewFromLibrarian(PurchasedMovie purchasedMovie);

    @Mapping(target = "id", source = "movie.id")
    @Mapping(target = "title", source = "movie.title")
    @Mapping(target = "purchasedAt", source = "purchasedAt")
    @Mapping(target = "priceSnapshot", source = "priceSnapshot")
    @Mapping(target = "genre", source = "movie.genre")
    @Mapping(target = "posterUrl", source = "movie.posterUrl")
    @Mapping(target = "rating", source = "movie.rating")
    @Mapping(target = "releaseDate", source = "movie.releaseDate")
    @Mapping(target = "director", source = "movie.director")
    @Mapping(target = "formattedDuration", source = "movie.duration",
            qualifiedByName = "durationToString")
    HistoricalMovieForOwnerViewDTO toOwnerViewFromHistorical(PurchasedMovie purchasedMovie);

}