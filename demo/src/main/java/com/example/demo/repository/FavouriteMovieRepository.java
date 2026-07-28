package com.example.demo.repository;

import com.example.demo.model.FavouriteMovie;
import org.springframework.stereotype.Repository;

@Repository
public interface FavouriteMovieRepository extends AbstractLinkedCollectionRepository<FavouriteMovie> {
}