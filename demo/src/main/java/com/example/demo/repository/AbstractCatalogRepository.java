package com.example.demo.repository;

import com.example.demo.model.AbstractCatalogItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

@NoRepositoryBean
public interface AbstractCatalogRepository<T extends AbstractCatalogItem> extends JpaRepository<T, Long> {

    @EntityGraph(attributePaths = {
            "movie",
            "user"
    })
    @Query("""
            SELECT item
            FROM #{#entityName} item
            WHERE item.user.username = :username
            """)
    List<T> findAllByUsername(@Param("username") String username);

    @EntityGraph(attributePaths = {
            "movie",
            "user"
    })
    @Query("""
            SELECT item
            FROM #{#entityName} item
            WHERE item.movie.id = :movieId
            """)
    List<T> findAllByMovieId(@Param("movieId")
                             Long movieId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            DELETE FROM #{#entityName} item
            WHERE item.movie.id = :movieId
            AND item.user.username = :username
            """)
    int deleteByMovieIdAndUserUsername(@Param("movieId")
                                       Long movieId,
                                       @Param("username")
                                       String username);

    @Query("""
            SELECT item
            FROM #{#entityName} item
            JOIN FETCH item.movie
            JOIN FETCH item.user
            WHERE item.user.username = :username
            AND item.movie.id = :movieId
            """)
    Optional<T> findByMovieIdAndUserUsername(@Param("movieId")
                                             Long movieId,
                                             @Param("username")
                                             String username);

    @Lock(PESSIMISTIC_WRITE)
    @Query("""
            SELECT item
            FROM #{#entityName} item
            WHERE item.movie.id = :movieId
            AND item.user.username = :username
            """)
    Optional<T> findByMovieIdAndUserUsernameWithLock(@Param("movieId")
                                                     Long movieId,
                                                     @Param("username")
                                                     String username);

    @Query("""
            SELECT COUNT(item)
            FROM #{#entityName} item
            WHERE item.user.username = :username
            """)
    Long countByUserUsername(@Param("username")
                             String username);

    @Query("""
            SELECT COUNT(item)
            FROM #{#entityName} item
            WHERE item.movie.id = :movieId
            """)
    Long countByMovieId(@Param("movieId")
                        Long movieId);

    @Query("""
            SELECT COUNT(item) > 0
            FROM #{#entityName} item
            WHERE item.movie.id = :movieId
            AND item.user.username = :username
            """)
    boolean existsByMovieIdAndUserUsername(@Param("movieId")
                                           Long movieId,
                                           @Param("username")
                                           String username);
}