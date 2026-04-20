package com.example.demo.repository;

import com.example.demo.model.PurchasedMovie;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchasedMovieRepository extends JpaRepository<PurchasedMovie, Long>,
        JpaSpecificationExecutor<PurchasedMovie> {

    @EntityGraph(attributePaths = {
            "movie",
            "user",
            "movie.purchases",
            "movie.ratings",
            "movie.favourites"
    })
    @Query("""
            SELECT DISTINCT purchasedMovie
            FROM PurchasedMovie purchasedMovie
            WHERE purchasedMovie.user.username = :username
            """)
    Page<PurchasedMovie> findAllByUsername(Pageable pageable,
                                           @Param("username") String username);

    @Modifying
    @Query("""
            DELETE FROM PurchasedMovie purchasedMovie
            WHERE purchasedMovie.movie.title = :title
            AND purchasedMovie.user.username = :username
            """)
    void deleteByTitleAndUsername(@Param("title") String title,
                                  @Param("username") String username);

    @EntityGraph(attributePaths = {
            "user",
            "movie"
    })
    @Query("""
                SELECT DISTINCT purchasedMovie
                FROM PurchasedMovie purchasedMovie
                WHERE purchasedMovie.movie.title = :title
                AND purchasedMovie.user.username = :username
            """)
    Optional<PurchasedMovie> findFullByUsernameAndTitle(@Param("username") String username,
                                                        @Param("title") String title);

    default PurchasedMovie findFullByUsernameAndTitleOrThrow(String username, String title) {
        return findFullByUsernameAndTitle(username, title)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Купленный фильм не найден: пользователь '" + username + "', " +
                                "фильм '" + title + "'"));
    }

    boolean existsByMovieTitleAndUserUsername(String movieTitle,
                                              String userUsername);

    List<PurchasedMovie> findAllByUserUsername(String username);
}