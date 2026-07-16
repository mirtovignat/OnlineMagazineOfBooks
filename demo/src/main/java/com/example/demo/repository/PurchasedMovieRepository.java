package com.example.demo.repository;

import com.example.demo.model.PurchasedMovie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PurchasedMovieRepository extends JpaRepository<PurchasedMovie, Long> {

    @EntityGraph(attributePaths = {
            "movie",
            "user"
    })
    @Query("""
            SELECT purchasedMovie
            FROM PurchasedMovie purchasedMovie
            WHERE purchasedMovie.user.username = :username
            """)
    Page<PurchasedMovie> findAllByUsername(Pageable pageable,
                                           @Param("username")
                                           String username);

    void deleteByUserUsername(String username);

    boolean existsByMovieIdAndUserUsername(Long movieId,
                                           String username);

    @Query("""
            SELECT purchasedMovie.movie.id
            FROM PurchasedMovie purchasedMovie
            WHERE purchasedMovie.user.username = :username
            """)
    List<Long> findMovieIdsByUsername(@Param("username")
                                      String username);
}