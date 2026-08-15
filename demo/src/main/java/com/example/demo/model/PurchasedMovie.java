package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

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
public class PurchasedMovie extends AbstractCatalogItem {
    @Column(name = "price_snapshot", nullable = false)
    private BigDecimal priceSnapshot;

}

