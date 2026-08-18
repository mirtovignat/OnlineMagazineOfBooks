package com.example.demo.repository;

import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    default User findByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND, "id=" + id));
    }

    default String findUsernameById(Long id) {
        return findById(id)
                .map(User::getUsername)
                .orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND, "id=" + id));
    }

    @Query("""
            SELECT user
            FROM User user
            WHERE user.username = :username
            """)
    Optional<User> findByUsername(@Param("username") String username);

    default User findByUsernameOrThrow(String username) {
        return findByUsername(username)
                .orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND, username));
    }

    @Query("""
            SELECT user
            FROM User user
            WHERE user.email = :email
            """)
    Optional<User> findByEmail(@Param("email") String email);

    @Query("""
            SELECT user
            FROM User user
            WHERE user.phone = :phone
            """)
    Optional<User> findByPhone(@Param("phone") String phone);

    @Query("""
            SELECT COUNT(user) > 0
            FROM User user
            WHERE user.email = :email
            """)
    boolean existsByEmail(@Param("email") String email);

    @Query("""
            SELECT COUNT(user) > 0
            FROM User user
            WHERE user.phone = :phone
            """)
    boolean existsByPhone(@Param("phone") String phone);

    @Query("""
            SELECT COUNT(user) > 0
            FROM User user
            WHERE user.username = :username
            """)
    boolean existsByUsername(@Param("username") String username);

    @Query("""
            SELECT COUNT(purchasedMovie)
            FROM PurchasedMovie purchasedMovie
            WHERE purchasedMovie.user.id = :userId
            """)
    long countPurchasesByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT COUNT(ratedMovie)
            FROM RatedMovie ratedMovie
            WHERE ratedMovie.user.id = :userId
            """)
    long countRatingsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("""
            UPDATE User user
            SET user.deleted = true,
                user.username = CONCAT('deleted_', user.id)
            WHERE user.id = :userId
            """)
    void softDeleteByUserId(@Param("userId") Long userId);
}