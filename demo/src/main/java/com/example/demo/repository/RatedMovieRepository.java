package com.example.demo.repository;

import com.example.demo.model.Movie;
import com.example.demo.model.RatedMovie;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface RatedMovieRepository extends AbstractCatalogRepository<RatedMovie> {

    @Query("""
            SELECT AVG(ratedMovie.ratingValue)
            FROM RatedMovie ratedMovie
            WHERE ratedMovie.movie = :movie
            """)
    BigDecimal calculateAverageRating(@Param("movie")
                                      Movie movie);
}