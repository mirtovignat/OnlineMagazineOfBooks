package com.example.demo.repository;

import com.example.demo.model.AbstractCatalogItem;
import com.example.demo.model.AbstractCatalogItemId;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

@NoRepositoryBean
public interface AbstractLinkedCollectionRepository<T extends AbstractCatalogItem>
        extends JpaRepository<T, AbstractCatalogItemId> {

    @EntityGraph(attributePaths = {"movie", "user"})
    @Query("""
            SELECT DISTINCT item
            FROM #{#entityName} item
            WHERE item.id.userId = :userId
            """)
    List<T> findAllByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT COUNT(item) > 0
            FROM #{#entityName} item
            WHERE item.id.movieId = :movieId
            AND item.id.userId = :userId
            """)
    boolean existsByMovieIdAndUserId(@Param("movieId") Long movieId,
                                     @Param("userId") Long userId);

    @Modifying
    @Query("""
            DELETE FROM #{#entityName} item
            WHERE item.id.movieId = :movieId
            AND item.id.userId = :userId
            """)
    void deleteByMovieIdAndUserId(@Param("movieId") Long movieId,
                                  @Param("userId") Long userId);

    @Modifying
    @Query("""
            DELETE FROM #{#entityName} item
            WHERE item.id.userId = :userId
            """)
    void deleteAllByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT COUNT(item)
            FROM #{#entityName} item
            WHERE item.id.userId = :userId
            """)
    Long countByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT item.id.movieId
            FROM #{#entityName} item
            WHERE item.id.userId = :userId
            """)
    List<Long> findMovieIdsByUserId(@Param("userId") Long userId);

    @Lock(PESSIMISTIC_WRITE)
    @Query("""
            SELECT item
            FROM #{#entityName} item
            WHERE item.id.movieId = :movieId
            AND item.id.userId = :userId
            """)
    Optional<T> findByMovieIdAndUserIdWithLock(@Param("movieId") Long movieId,
                                               @Param("userId") Long userId);
}