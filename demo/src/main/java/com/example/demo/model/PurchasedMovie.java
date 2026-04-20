package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "purchased_movies",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {
                        "user_id",
                        "movie_id"
                }
        )
)
public class PurchasedMovie extends AbstractEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(name = "purchased_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime purchasedAt;

    @Column(name = "price_snapshot", nullable = false)
    private BigDecimal priceSnapshot;

}

