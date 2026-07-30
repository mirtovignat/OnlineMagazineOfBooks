package com.example.demo.repository;

import com.example.demo.model.Movie;
import com.example.demo.model.RatedMovie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

public interface RatedMovieRepository extends JpaRepository<RatedMovie, Long> {

    @EntityGraph(attributePaths = {
            "movie",
            "user"
    })
    @Query("""
            SELECT DISTINCT ratedMovie
            FROM RatedMovie ratedMovie
            WHERE ratedMovie.user.username = :username
            """)
    Page<RatedMovie> findAllByUsername(Pageable pageable,
                                       @Param("username") String username);


    @EntityGraph(attributePaths = {
            "movie",
            "user"
    })
    @Query("""
            SELECT DISTINCT ratedMovie
            FROM RatedMovie ratedMovie
            WHERE ratedMovie.movie.id = :movieId
            """)
    Page<RatedMovie> findAllByMovieId(@Param("movieId") Long movieId,
                                      Pageable pageable);

    @Modifying
    @Query("""
            DELETE FROM RatedMovie ratedMovie
            WHERE ratedMovie.movie.id = :movieId
            AND ratedMovie.user.username = :username
            """)
    void deleteByMovieIdAndUsername(@Param("movieId") Long movieId,
                                    @Param("username") String username);

    @Modifying
    @Query("""
            DELETE FROM RatedMovie ratedMovie
            WHERE ratedMovie.user.username = :username
            """)
    void deleteAllByUsername(@Param("username") String username);


    @Query("""
            SELECT ratedMovie
            FROM RatedMovie ratedMovie
            JOIN FETCH ratedMovie.movie
            JOIN FETCH ratedMovie.user
            WHERE ratedMovie.user.username = :username
            AND ratedMovie.movie.id = :movieId
            """)
    Optional<RatedMovie> findByMovieIdAndUserUsername(@Param("movieId") Long movieId,
                                                      @Param("username") String username);

    @Lock(PESSIMISTIC_WRITE)
    @Query("""
            SELECT ratedMovie
            FROM RatedMovie ratedMovie
            WHERE ratedMovie.movie.id = :movieId
            AND ratedMovie.user.username = :username
            """)
    Optional<RatedMovie> findByMovieIdAndUserUsernameWithLock(@Param("movieId") Long movieId,
                                                              @Param("username") String username);

    @Query("""
            SELECT AVG(ratedMovie.ratingValue)
            FROM RatedMovie ratedMovie
            WHERE ratedMovie.movie = :movie
            """)
    BigDecimal calculateAverageRating(Movie movie);

    boolean existsByMovieTitleAndUserUsername(String title, String username);

    long countByUserUsername(String username);

    long countByMovieId(Long movieId);
}