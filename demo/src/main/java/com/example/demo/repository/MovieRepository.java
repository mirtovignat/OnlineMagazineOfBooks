package com.example.demo.repository;

import com.example.demo.model.Movie;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {


    @EntityGraph(attributePaths = {"purchases", "favourites", "ratings", "cartItems"})
    @Query("""
            SELECT DISTINCT movie
            FROM Movie movie
            WHERE movie.title = :title
            """)
    Optional<Movie> findFullByTitle(@Param("title") String title);

    default Movie findFullByTitleOrThrow(String title) {
        return findFullByTitle(title)
                .orElseThrow(() -> new EntityNotFoundException("Фильм не найден по названию: "
                        + title));
    }

    @EntityGraph(attributePaths = {"purchases"})
    @Query("""
            SELECT DISTINCT movie
            FROM Movie movie
            """)
    Page<Movie> findAllWithDirectorPurchasesAndReviews(Pageable pageable);

    @Query("""
            SELECT movie
            FROM Movie movie
            WHERE movie.title IN :titles
            """)
    List<Movie> findAllByTitles(@Param("titles") List<String> titles);
}