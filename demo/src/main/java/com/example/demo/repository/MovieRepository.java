package com.example.demo.repository;

import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @EntityGraph(attributePaths = {"purchases"})
    @Query("""
            SELECT DISTINCT movie
            FROM Movie movie
            """)
    Page<Movie> findAllWithDirectorPurchasesAndReviews(Pageable pageable);

    @Query("""
            SELECT movie
            FROM Movie movie
            WHERE (:title IS NULL OR :title = '' OR LOWER(movie.title) LIKE LOWER(CONCAT('%', :title, '%')))
              AND (:releaseYear IS NULL OR EXTRACT(YEAR FROM movie.releaseDate) = :releaseYear)
              AND (:genre IS NULL OR LOWER(movie.genre) = LOWER(CAST(:genre AS string)))
              AND (:director IS NULL OR LOWER(movie.director) = LOWER(CAST(:director AS string)))
            """)
    Page<Movie> findByOptionalParams(
            @Param("title") String title,
            @Param("releaseYear") Integer releaseYear,
            @Param("genre") String genre,
            @Param("director") String director,
            Pageable pageable
    );

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

    @Query("""
            SELECT movie FROM Movie movie
            WHERE movie.title = :title
              AND movie.director = :director
              AND movie.genre = :genre
              AND (:releaseYear IS NULL OR YEAR(movie.releaseDate) = :releaseYear)
            """)
    List<Movie> findByTitleAndDirectorAndGenreAndReleaseYear(
            @Param("title") String title,
            @Param("director") String director,
            @Param("genre") String genre,
            @Param("releaseYear") Integer releaseYear
    );

    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}