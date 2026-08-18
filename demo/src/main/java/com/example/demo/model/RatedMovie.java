package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "rated_movies",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "movie_id"}))
public class RatedMovie extends AbstractCatalogItem {

    @Column(name = "review")
    private String review;

    @DecimalMin(value = "0.1", message = "Оценка не может быть меньше 0.1")
    @DecimalMax(value = "10.0", message = "Оценка не может быть больше 10")
    @Column(name = "rating_value", nullable = false, precision = 4, scale = 2)
    private BigDecimal ratingValue;
}