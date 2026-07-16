package com.example.demo.repository;

import com.example.demo.model.FavouriteMovie;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

public interface FavouriteMovieRepository extends JpaRepository<FavouriteMovie, Long> {

    @EntityGraph(attributePaths = {
            "movie",
            "user"
    })
    @Query("""
            SELECT DISTINCT favouriteMovie
            FROM FavouriteMovie favouriteMovie
            WHERE favouriteMovie.user.username = :username
            """)
    List<FavouriteMovie> findAllByUsername(@Param("username")
                                           String username);


    boolean existsByMovieTitleAndUserUsername(String movieTitle, String userUsername);

    boolean existsByMovieIdAndUserUsername(Long movieId, String userUsername);

    @Modifying
    @Query("""
            DELETE FROM FavouriteMovie favouriteMovie
            WHERE favouriteMovie.movie.id = :movieId
            AND favouriteMovie.user.username = :username
            """)
    void deleteByMovieIdAndUserUsername(@Param("movieId")
                                        Long movieId,
                                        @Param("username")
                                        String username);

    @Modifying
    @Query("""
            DELETE FROM FavouriteMovie favouriteMovie
            WHERE favouriteMovie.user.username = :username
            """)
    void deleteAllByUsername(@Param("username")
                             String username);

    @Query("""
            SELECT COUNT(favouriteMovie)
            FROM FavouriteMovie favouriteMovie
            WHERE favouriteMovie.user.username = :username
            """)
    int countByUsername(@Param("username")
                        String username);

    @Query("""
            SELECT favouriteMovie.movie.id
            FROM FavouriteMovie favouriteMovie
            WHERE favouriteMovie.user.username = :username
            """)
    List<Long> findMovieIdsByUsername(@Param("username")
                                      String username);

    @Lock(PESSIMISTIC_WRITE)
    @Query("""
            SELECT favouriteMovie
            FROM FavouriteMovie favouriteMovie
            WHERE favouriteMovie.movie.id = :movieId
            AND favouriteMovie.user.username = :username
            """)
    Optional<FavouriteMovie> findByMovieIdAndUserUsernameWithLock(@Param("movieId") Long movieId,
                                                                  @Param("username") String username);
}