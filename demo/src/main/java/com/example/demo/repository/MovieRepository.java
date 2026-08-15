package com.example.demo.repository;

import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {

    default Movie findByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> BusinessException.of(ErrorCode.ENTITY_NOT_FOUND));
    }

    @Query("""
            SELECT movie.rating
            FROM Movie movie
            WHERE movie.id = :id
            """)
    Optional<Double> findRatingById(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT movie.director
            FROM Movie movie
            WHERE movie.director IS NOT NULL
            ORDER BY movie.director
            """)
    List<String> findAllDistinctDirectors();

    @Query("""
            SELECT DISTINCT movie.genre
            FROM Movie movie
            WHERE movie.genre IS NOT NULL
            ORDER BY movie.genre
            """)
    List<String> findAllDistinctGenres();
}