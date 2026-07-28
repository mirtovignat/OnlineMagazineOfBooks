package com.example.demo.repository;

import com.example.demo.model.AbstractLinkedCollectionItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

@NoRepositoryBean
public interface AbstractLinkedCollectionRepository<T extends AbstractLinkedCollectionItem>
        extends JpaRepository<T, Long> {

    @EntityGraph(attributePaths = {
            "movie",
            "user"
    })
    @Query("""
            SELECT DISTINCT item
            FROM #{#entityName} item
            WHERE item.user.username = :username
            """)
    List<T> findAllByUsername(@Param("username")
                              String username);

    boolean existsByMovieTitleAndUserUsername(String movieTitle, String userUsername);

    boolean existsByMovieIdAndUserUsername(Long movieId, String userUsername);

    @Modifying
    @Query("""
            DELETE FROM #{#entityName} item
            WHERE item.movie.id = :movieId
            AND item.user.username = :username
            """)
    void deleteByMovieIdAndUserUsername(@Param("movieId")
                                        Long movieId,
                                        @Param("username")
                                        String username);

    @Modifying
    @Query("""
            DELETE FROM #{#entityName} item
            WHERE item.user.username = :username
            """)
    void deleteAllByUsername(@Param("username")
                             String username);

    @Query("""
            SELECT COUNT(item)
            FROM #{#entityName} item
            WHERE item.user.username = :username
            """)
    int countByUsername(@Param("username")
                        String username);

    @Query("""
            SELECT item.movie.id
            FROM #{#entityName} item
            WHERE item.user.username = :username
            """)
    List<Long> findMovieIdsByUsername(@Param("username")
                                      String username);

    @Lock(PESSIMISTIC_WRITE)
    @Query("""
            SELECT item
            FROM #{#entityName} item
            WHERE item.movie.id = :movieId
            AND item.user.username = :username
            """)
    Optional<T> findByMovieIdAndUserUsernameWithLock(@Param("movieId") Long movieId,
                                                     @Param("username") String username);
}