package com.example.demo.repository;

import com.example.demo.model.PurchasedMovie;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchasedMovieRepository extends AbstractCatalogRepository<PurchasedMovie> {

    @Query("""
            SELECT COUNT(purchasedMovie) > 0
            FROM PurchasedMovie purchasedMovie
            WHERE purchasedMovie.movie.id = :movieId
            AND purchasedMovie.user.username = :username
            """)
    boolean existsByMovieIdAndUserUsername(@Param("movieId") Long movieId,
                                           @Param("username") String username);

    @Query("""
            SELECT purchasedMovie.movie.id
            FROM PurchasedMovie purchasedMovie
            WHERE purchasedMovie.user.username = :username
            """)
    List<Long> findMovieIdsByUsername(@Param("username")
                                      String username);

}