package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "favourite_movies",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {
                        "user_id",
                        "movie_id"
                }
        )
)
public class FavouriteMovie extends AbstractLinkedCollectionItem {
}

