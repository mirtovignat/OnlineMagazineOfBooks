package com.example.demo.repository.entity;

import com.example.demo.model.entity.Movie;
import com.example.demo.model.entity.RatedMovie;
import com.example.demo.repository.base.AbstractCatalogRepository;
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