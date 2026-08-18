package com.example.demo.service.precence;

import com.example.demo.model.FavouriteMovie;
import com.example.demo.repository.FavouriteMovieRepository;
import org.springframework.stereotype.Service;

@Service
public class FavouritePresenceService extends PresenceService<FavouriteMovie> {
    public FavouritePresenceService(FavouriteMovieRepository favouriteMovieRepository) {
        super(favouriteMovieRepository);
    }
}