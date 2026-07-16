package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "rated_movies",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "movie_id"}
        )
)
public class RatedMovie extends AbstractEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(name = "review")
    private String review;

    @Column(name = "rated_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime ratedAt;

    @DecimalMin(value = "0.0", message = "Оценка не может быть меньше 0")
    @DecimalMax(value = "10.0", message = "Оценка не может быть больше 10")
    @Column(name = "rating_value", nullable = false, precision = 4, scale = 2)
    private BigDecimal ratingValue;
}
