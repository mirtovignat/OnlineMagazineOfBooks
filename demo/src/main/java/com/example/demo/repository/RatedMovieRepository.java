package com.example.demo.repository;

import com.example.demo.model.Movie;
import com.example.demo.model.RatedMovie;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
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
    Page<RatedMovie> findAllByUsername(Pageable pageable, @Param("username") String username);

    @EntityGraph(attributePaths = {
            "movie",
            "user"
    })
    @Query("""
            SELECT DISTINCT ratedMovie
            FROM RatedMovie ratedMovie
            WHERE ratedMovie.movie.title = :title
            """)
    List<RatedMovie> findAllByMovieTitle(@Param("title") String title);

    @Modifying
    @Query("""
            DELETE FROM RatedMovie ratedMovie
            WHERE ratedMovie.movie.title = :title
            AND ratedMovie.user.username = :username
            """)
    void deleteByTitleAndUsername(@Param("title") String title,
                                  @Param("username") String username);

    @Modifying
    @Query("""
            DELETE FROM RatedMovie ratedMovie
            WHERE ratedMovie.user.username = :username
            """)
    void deleteAllByUsername(@Param("username") String username);

    @EntityGraph(attributePaths = {
            "movie",
            "user"
    })
    @Query("""
            SELECT ratedMovie
            FROM RatedMovie ratedMovie
            JOIN FETCH ratedMovie.movie
            JOIN FETCH ratedMovie.user
            WHERE ratedMovie.user.username = :username
            AND ratedMovie.movie.title = :title
            """)
    Optional<RatedMovie> findByUsernameAndTitle(@Param("username") String username,
                                                @Param("title") String title);

    default RatedMovie findByUsernameAndTitleOrThrow(
            String username, String title) {
        return findByUsernameAndTitle(username,
                title)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Оценённый фильм не найден: пользователь '"
                                + username + "', фильм '"
                                + title + "'"));
    }

    boolean existsByUserUsernameAndMovieTitle(String userUsername, String movieTitle);

    @Query("""
            SELECT AVG(ratedMovie.ratingValue)
            FROM RatedMovie ratedMovie
            WHERE ratedMovie.movie = :movie
            """)
    BigDecimal calculateAverageRating(Movie movie);
}