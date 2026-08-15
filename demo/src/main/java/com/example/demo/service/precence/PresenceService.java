package com.example.demo.service.precence;

import com.example.demo.model.AbstractLinkedCollectionItem;
import com.example.demo.repository.AbstractLinkedCollectionRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PresenceService<Entity extends AbstractLinkedCollectionItem> {

    private final AbstractLinkedCollectionRepository<Entity> linkedCollectionRepository;

    public boolean isInLinkedCollection(Long id, String username) {
        return linkedCollectionRepository
                .existsByMovieIdAndUserUsername(id, username);
    }

}