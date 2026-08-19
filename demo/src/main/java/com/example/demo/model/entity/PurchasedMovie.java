package com.example.demo.model.entity;

import com.example.demo.model.base.AbstractCatalogItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "purchased_movies",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "movie_id"}))
public class PurchasedMovie extends AbstractCatalogItem {

    @Column(name = "price_snapshot", nullable = false)
    private BigDecimal priceSnapshot;
}