package com.example.demo.repository.base;

import com.example.demo.model.base.AbstractCatalogItem;
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
            WHERE item.user.id = :userId
            """)
    List<T> findAllByUserId(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {
            "movie",
            "user"
    })
    @Query("""
            SELECT item
            FROM #{#entityName} item
            WHERE item.movie.id = :movieId
            """)
    List<T> findAllByMovieId(@Param("movieId") Long movieId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            DELETE FROM #{#entityName} item
            WHERE item.movie.id = :movieId
            AND item.user.id = :userId
            """)
    int deleteByMovieIdAndUserId(@Param("movieId") Long movieId,
                                 @Param("userId") Long userId);

    @Query("""
            SELECT item
            FROM #{#entityName} item
            JOIN FETCH item.movie
            JOIN FETCH item.user
            WHERE item.user.id = :userId
            AND item.movie.id = :movieId
            """)
    Optional<T> findByMovieIdAndUserId(@Param("movieId") Long movieId,
                                       @Param("userId") Long userId);

    @Lock(PESSIMISTIC_WRITE)
    @Query("""
            SELECT item
            FROM #{#entityName} item
            WHERE item.movie.id = :movieId
            AND item.user.id = :userId
            """)
    Optional<T> findByMovieIdAndUserIdWithLock(@Param("movieId") Long movieId,
                                               @Param("userId") Long userId);

    @Query("""
            SELECT COUNT(item)
            FROM #{#entityName} item
            WHERE item.user.id = :userId
            """)
    Long countByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT COUNT(item)
            FROM #{#entityName} item
            WHERE item.movie.id = :movieId
            """)
    Long countByMovieId(@Param("movieId") Long movieId);

    @Query("""
            SELECT COUNT(item) > 0
            FROM #{#entityName} item
            WHERE item.movie.id = :movieId
            AND item.user.id = :userId
            """)
    boolean existsByMovieIdAndUserId(@Param("movieId") Long movieId,
                                     @Param("userId") Long userId);
}