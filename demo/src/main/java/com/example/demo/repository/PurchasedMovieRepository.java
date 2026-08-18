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
            AND purchasedMovie.user.id = :userId
            """)
    boolean existsByMovieIdAndUserId(@Param("movieId") Long movieId,
                                     @Param("userId") Long userId);

    @Query("""
            SELECT purchasedMovie.movie.id
            FROM PurchasedMovie purchasedMovie
            WHERE purchasedMovie.user.id = :userId
            """)
    List<Long> findMovieIdsByUserId(@Param("userId")
                                    Long userId);

}