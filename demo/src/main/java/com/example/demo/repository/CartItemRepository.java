package com.example.demo.repository;

import com.example.demo.model.CartItem;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @EntityGraph(attributePaths = "movie")
    @Query("""
            SELECT DISTINCT cartItem
            FROM CartItem cartItem
            WHERE cartItem.user.username = :username
            """)
    List<CartItem> findAllByUsername(@Param("username") String username);

    @Modifying
    @Query("""
            DELETE FROM CartItem cartItem
            WHERE cartItem.movie.title = :title
            AND cartItem.user.username = :username
            """)
    void deleteByTitleAndUsername(@Param("title") String title, @Param("username") String username);

    @Modifying
    @Query("""
            DELETE FROM CartItem cartItem
            WHERE cartItem.user.username = :username
            """)
    void deleteAllByUsername(@Param("username") String username);

    boolean existsByMovieTitleAndUserUsername(String title, String username);

    @Query("""
            SELECT COUNT(cartItem)
            FROM CartItem cartItem
            WHERE cartItem.user.username = :username
            """)
    int countByUsername(@Param("username") String username);

    @EntityGraph(attributePaths = {"movie", "user"})
    @Query("""
            SELECT cartItem
            FROM CartItem cartItem
            WHERE cartItem.movie.title = :title
            AND cartItem.user.username = :username
            """)
    Optional<CartItem> findFullByUsernameAndTitle(@Param("username") String username, @Param("title") String title);

    default CartItem findFullByUsernameAndTitleOrThrow(String username, String title) {
        return findFullByUsernameAndTitle(username, title)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Товар в корзине не найден: пользователь '" + username + "', фильм '" + title + "'"));
    }
}