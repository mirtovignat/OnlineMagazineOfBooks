package com.example.demo.service.linked_collection;

import com.example.demo.dto.badges.BadgeCountsDTO;
import com.example.demo.dto.joined_to_user.FavouriteMovieForOwnerViewDTO;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.FavouritesMapper;
import com.example.demo.model.FavouriteMovie;
import com.example.demo.model.Movie;
import com.example.demo.model.User;
import com.example.demo.repository.FavouriteMovieRepository;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.PurchasedMovieRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.precence.FavouritePresenceService;
import org.springframework.stereotype.Service;

@Service
public class FavouritesService extends AbstractLinkedCollectionService<FavouriteMovie,
        FavouriteMovieForOwnerViewDTO> {

    private final FavouritesMapper favouritesMapper;
    private final FavouritePresenceService favouriteMoviePresenceService;

    public FavouritesService(FavouriteMovieRepository favouriteMovieRepository,
                             UserRepository userRepository,
                             MovieRepository movieRepository,
                             PurchasedMovieRepository purchasedMovieRepository,
                             FavouritesMapper favouritesMapper,
                             FavouritePresenceService favouriteMoviePresenceService) {
        super(favouriteMovieRepository, userRepository, movieRepository,
                purchasedMovieRepository);
        this.favouritesMapper = favouritesMapper;
        this.favouriteMoviePresenceService = favouriteMoviePresenceService;
    }

    @Override
    public BadgeCountsDTO updateBadge(BadgeCountsDTO badgeCountsDTO, Long newCount) {
        return badgeCountsDTO.toBuilder().favouritesCount(newCount).build();
    }

    @Override
    protected FavouriteMovie createEntity(User user, Movie movie) {
        return new FavouriteMovie();
    }

    @Override
    protected ErrorCode getEmptyErrorCode() {
        return ErrorCode.EMPTY_FAVOURITES;
    }

    @Override
    protected FavouriteMovieForOwnerViewDTO mapToDto(FavouriteMovie favouriteMovie) {
        return favouritesMapper.toOwnerView(favouriteMovie, favouriteMoviePresenceService);
    }
}