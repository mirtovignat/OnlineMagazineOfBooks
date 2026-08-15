package com.example.demo.repository;

import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Lock(PESSIMISTIC_WRITE)
    @Query("SELECT user FROM User user WHERE user.username = :username")
    User findByUsernameWithLock(@Param("username") String username);

    Optional<User> findByUsername(String username);

    default User findByUsernameOrThrow(String username) {
        return findByUsername(username)
                .orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND, username));
    }

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByUsername(String username);

    @Query("SELECT COUNT(p) FROM PurchasedMovie p WHERE p.user.username = :username")
    long countPurchasesByUsername(@Param("username") String username);

    @Query("SELECT COUNT(r) FROM RatedMovie r WHERE r.user.username = :username")
    long countRatingsByUsername(@Param("username") String username);

    @Modifying
    @Query("""
            UPDATE User user
            SET user.deleted = true,
                user.username = '[deleted]',
                user.email = null,
                user.phone = null
            WHERE user.username = :username
            """)
    void softDeleteByUsername(@Param("username") String username);
}