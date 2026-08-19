package com.example.demo.repository.entity;

import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Modifying
    @Query("""
            UPDATE User user
            SET user.deleted = true,
                user.username = CONCAT('deleted_', user.id, '_', :suffix),
                user.email = CONCAT('deleted_', user.id, '_', :suffix, '_', COALESCE(user.email, 'null')),
                user.phone = CONCAT('deleted_', user.id, '_', :suffix, '_', COALESCE(user.phone, 'null'))
            WHERE user.id = :userId
            """)
    void softDeleteById(@Param("userId") Long userId, @Param("suffix") String suffix);

    default User findByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND, "id=" + id));
    }

    @Query("""
            SELECT user
            FROM User user
            WHERE user.username = :username
            AND user.deleted = false
            """)
    Optional<User> findByUsername(@Param("username") String username);

    @Query("""
            SELECT user
            FROM User user
            WHERE user.email = :email
            AND user.deleted = false
            """)
    Optional<User> findByEmail(@Param("email") String email);

    @Query("""
            SELECT user
            FROM User user
            WHERE user.phone = :phone
            AND user.deleted = false
            """)
    Optional<User> findByPhone(@Param("phone") String phone);

    @Query("""
            SELECT COUNT(user) > 0
            FROM User user
            WHERE user.email = :email
            AND user.deleted = false
            """)
    boolean existsByEmail(@Param("email") String email);

    @Query("""
            SELECT COUNT(user) > 0
            FROM User user
            WHERE user.phone = :phone
            AND user.deleted = false
            """)
    boolean existsByPhone(@Param("phone") String phone);

    @Query("""
            SELECT COUNT(user) > 0
            FROM User user
            WHERE user.username = :username
            AND user.deleted = false
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
}