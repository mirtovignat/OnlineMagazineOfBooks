package com.example.demo.repository;

import com.example.demo.model.FavouriteMovie;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavouriteMovieRepository extends JpaRepository<FavouriteMovie, Long>,
        JpaSpecificationExecutor<FavouriteMovie> {


    @EntityGraph(attributePaths = {"movie", "user"})
    @Query("""
            SELECT DISTINCT favouriteMovie
            FROM FavouriteMovie favouriteMovie
            WHERE favouriteMovie.user.username = :username
            """)
    List<FavouriteMovie> findAllByUsername(@Param("username") String username);

    @EntityGraph(attributePaths = {"movie", "user"})
    @Query("""
            SELECT favouriteMovie
            FROM FavouriteMovie favouriteMovie
            JOIN FETCH favouriteMovie.movie
            WHERE favouriteMovie.user.username = :username
            AND favouriteMovie.movie.title = :title
            """)
    Optional<FavouriteMovie> findByUsernameAndTitle(@Param("username") String username,
                                                    @Param("title") String title);

    default FavouriteMovie findByUsernameAndTitleOrThrow(String username, String title) {
        return findByUsernameAndTitle(username, title)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Избранный фильм не найден: пользователь '"
                                + username + "', фильм '" + title + "'"));
    }

    boolean existsByMovieTitleAndUserUsername(String movieTitle, String userUsername);

    @Modifying
    @Query("""
            DELETE FROM FavouriteMovie favouriteMovie
            WHERE favouriteMovie.movie.title = :title
            AND favouriteMovie.user.username = :username
            """)
    void deleteByMovieTitleAndUserUsername(@Param("title") String title,
                                           @Param("username") String username);

    @Query("""
            SELECT COUNT(favouriteMovie)
            FROM FavouriteMovie favouriteMovie
            WHERE favouriteMovie.user.username = :username
            """)
    int countByUsername(@Param("username") String username);
}