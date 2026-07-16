package com.example.demo.repository;

import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Lock(PESSIMISTIC_WRITE)
    @Query("""
            SELECT user
            FROM User user
            WHERE user.username = :username
            """)
    User findByUsernameWithLock(@Param("username") String username);

    @EntityGraph(attributePaths = {
            "purchases",
            "favourites",
            "ratings",
            "cartItems"
    })
    @Query("""
            SELECT DISTINCT user
            FROM User user
            """)
    @NonNull
    Page<User> findAll(@NonNull Pageable pageable);

    @EntityGraph(attributePaths = {
            "purchases",
            "favourites",
            "ratings",
            "cartItems"
    })
    @Query("""
            SELECT DISTINCT user
            FROM User user
            WHERE user.username = :username
            """)
    Optional<User> findByUsername(@Param("username") String username);

    default User findByUsernameOrThrow(String username) {
        return findByUsername(username)
                .orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND, username));
    }

    @EntityGraph(attributePaths = {
            "purchases",
            "favourites",
            "ratings",
            "cartItems"
    })
    @Query("""
            SELECT DISTINCT user
            FROM User user
            WHERE user.email = :email
            """)
    Optional<User> findByEmail(@Param("email") String email);


    @EntityGraph(attributePaths = {
            "purchases",
            "favourites",
            "ratings",
            "cartItems"
    })
    @Query("""
            SELECT DISTINCT user
            FROM User user
            WHERE user.phone = :phone
            """)
    Optional<User> findByPhone(@Param("phone") String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByUsername(String username);

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