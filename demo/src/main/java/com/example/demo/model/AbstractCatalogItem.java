package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractCatalogItem {

    @EmbeddedId
    private AbstractCatalogItemId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("movieId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    public static <Entity extends AbstractCatalogItem> Entity init(
            Entity entity,
            User user,
            Movie movie
    ) {
        entity.setUser(user);
        entity.setMovie(movie);
        AbstractCatalogItemId abstractCatalogItemId = AbstractCatalogItemId.from(movie, user);
        entity.setId(abstractCatalogItemId);
        return entity;
    }
}