package com.example.demo.model.base;

import com.example.demo.model.entity.Movie;
import com.example.demo.model.entity.User;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbstractCatalogItemId implements Serializable {

    private Long movieId;

    private Long userId;

    public static AbstractCatalogItemId from(Movie movie, User user) {
        return AbstractCatalogItemId.builder()
                .movieId(movie.getId())
                .userId(user.getId())
                .build();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        AbstractCatalogItemId that = (AbstractCatalogItemId) object;
        return Objects.equals(movieId, that.movieId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, userId);
    }
}