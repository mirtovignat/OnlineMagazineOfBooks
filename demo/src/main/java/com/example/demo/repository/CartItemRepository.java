package com.example.demo.repository;

import com.example.demo.model.CartItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Lock(PESSIMISTIC_WRITE)
    @Query("""
            SELECT cartItem
            FROM CartItem cartItem
            JOIN FETCH cartItem.movie
            WHERE cartItem.user.username = :username
            """)
    List<CartItem> findAllByUsernameWithLock(@Param("username")
                                             String username);

    @EntityGraph(attributePaths = "movie")
    @Query("""
            SELECT DISTINCT cartItem
            FROM CartItem cartItem
            WHERE cartItem.user.username = :username
            """)
    List<CartItem> findAllByUsername(@Param("username")
                                     String username);

    @Modifying
    @Query("""
            DELETE FROM CartItem cartItem
            WHERE cartItem.movie.id = :movieId
            AND cartItem.user.username = :username
            """)
    void deleteByMovieIdAndUsername(@Param("movieId")
                                    Long movieId,
                                    @Param("username")
                                    String username);

    @Modifying
    @Query("""
            DELETE FROM CartItem cartItem
            WHERE cartItem.user.username = :username
            """)
    void deleteAllByUsername(@Param("username") String username);

    boolean existsByMovieTitleAndUserUsername(String title, String username);

    boolean existsByMovieIdAndUserUsername(Long movieId, String username);

    @Query("""
            SELECT COUNT(cartItem)
            FROM CartItem cartItem
            WHERE cartItem.user.username = :username
            """)
    int countByUsername(@Param("username")
                        String username);

    @Query("""
            SELECT cartItem.movie.id
            FROM CartItem cartItem
            WHERE cartItem.user.username = :username
            """)
    List<Long> findMovieIdsByUsername(@Param("username")
                                      String username);


    @Lock(PESSIMISTIC_WRITE)
    @Query("""
            SELECT cartItem
            FROM CartItem cartItem
            WHERE cartItem.movie.id = :movieId
            AND cartItem.user.username = :username
            """)
    Optional<CartItem> findByMovieIdAndUserUsernameWithLock(@Param("movieId") Long movieId,
                                                            @Param("username") String username);
}
