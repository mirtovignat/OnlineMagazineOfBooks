package com.example.demo.service;

import com.example.demo.dto.joined_to_user.CartMovieForOwnerViewDTO;
import com.example.demo.dto.joined_to_user.FavouriteMovieForOwnerViewDTO;
import com.example.demo.dto.movie.*;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.filter.MovieFilter;
import com.example.demo.mapper.MovieMapper;
import com.example.demo.model.CartItem;
import com.example.demo.model.FavouriteMovie;
import com.example.demo.model.Movie;
import com.example.demo.repository.MovieRepository;
import com.example.demo.utils.MovieSearchHelper;
import com.example.demo.utils.MovieUserStatusHelper;
import com.example.demo.utils.PaginationHelper;
import com.example.demo.utils.SqlSearchEscaper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;
    private final PurchasedService purchasedService;
    private final AbstractLinkedCollectionService<FavouriteMovie, FavouriteMovieForOwnerViewDTO> favouritesService;
    private final AbstractLinkedCollectionService<CartItem, CartMovieForOwnerViewDTO> cartService;
    private final SqlSearchEscaper sqlSearchEscaper;
    private final PaginationHelper paginationHelper;
    private final MovieUserStatusHelper userStatusHelper;
    private final MovieSearchHelper movieSearchHelper;

    public Double getMovieRating(Long id) {
        return movieRepository.findRatingById(id)
                .orElseThrow(() -> BusinessException.of(ErrorCode.ENTITY_NOT_FOUND));
    }

    @Transactional
    public MovieForUser<MovieCardDetailsViewDTO> getCard(Long movieId, String username) {
        MovieCardDetailsViewDTO movieCardDetailsViewDTO =
                movieMapper.toDetails(movieRepository.findByIdOrThrow(movieId));
        MovieUserStatus movieUserStatus = (username != null)
                ? calculateUserStatus(movieId, username)
                : new MovieUserStatus(false, false, false);
        return new MovieForUser<>(movieCardDetailsViewDTO, movieUserStatus);
    }

    public Page<MovieForUser<MovieCardViewDTO>> getMovieCards(String username, MovieSearchDTO movieSearchDTO, Pageable pageable) {
        Page<Movie> moviePage;
        if (movieSearchDTO != null) {
            String title = sqlSearchEscaper.prepareForSearch(movieSearchDTO.title());
            moviePage = (title == null) ? Page.empty(pageable) :
                    movieRepository.findByOptionalParams(title, movieSearchDTO.releaseYear(),
                            movieSearchDTO.genre(), movieSearchDTO.director(), pageable);
        } else {
            moviePage = movieRepository.findAllWithDirectorPurchasesAndReviews(pageable);
        }
        return enrichWithStatuses(moviePage, username);
    }

    public Page<MovieForUser<MovieCardViewDTO>> convertSearchDtoListToPage(List<MovieSearchDTO> movieSearchDTOS,
                                                                           Pageable pageable, String username) {
        List<Movie> matched = movieSearchHelper.findMoviesByDtoList(movieSearchDTOS);
        Page<Movie> moviePage = matched.isEmpty() ? Page.empty(pageable) : paginationHelper.paginateList(matched, pageable);
        return enrichWithStatuses(moviePage, username);
    }

    public Page<MovieForUser<MovieCardViewDTO>> convertMovieFilterToPage(MovieFilter movieFilter,
                                                                         Pageable pageable,
                                                                         String username) {
        Page<Movie> moviePage = movieSearchHelper.findMoviesByMovieFilter(movieFilter, pageable);
        return enrichWithStatuses(moviePage, username);
    }

    public List<MovieSearchDTO> getMovieSearchDTO(String movieSearchQuery) {
        return movieSearchHelper.getSuggestions(movieSearchQuery);
    }

    public List<String> getAllDirectors() {
        return movieRepository.findAllDistinctDirectors();
    }

    public List<String> getAllGenres() {
        return movieRepository.findAllDistinctGenres();
    }

    private MovieUserStatus calculateUserStatus(Long movieId, String username) {
        boolean isBought = purchasedService.isMoviePurchasedByBuyer(movieId, username);
        boolean isInCart = !isBought && cartService.existsInCollection(movieId, username);
        boolean isInFavourites = favouritesService.existsInCollection(movieId, username);
        return new MovieUserStatus(isBought, isInCart, isInFavourites);
    }

    private Page<MovieForUser<MovieCardViewDTO>> enrichWithStatuses(Page<Movie> moviePage, String username) {
        if (username != null) {
            return userStatusHelper.enrichWithUserStatuses(moviePage, username, movieMapper::toCard);
        }
        return moviePage.map(movie -> new MovieForUser<>(movieMapper.toCard(movie), new MovieUserStatus(false, false, false)));
    }


}