package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "favourite_movies",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "movie_id"}))
public class FavouriteMovie extends AbstractCatalogItem {
}