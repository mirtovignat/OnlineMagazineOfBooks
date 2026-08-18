package com.example.demo.service.precence;

import com.example.demo.model.AbstractCatalogItem;
import com.example.demo.repository.AbstractLinkedCollectionRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PresenceService<Entity extends AbstractCatalogItem> {

    private final AbstractLinkedCollectionRepository<Entity> linkedCollectionRepository;

    public boolean isInLinkedCollection(Long movieId, Long userId) {
        return linkedCollectionRepository
                .existsByMovieIdAndUserId(movieId, userId);
    }
}