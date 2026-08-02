package com.example.demo.service;

import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.model.AbstractLinkedCollectionItem;
import com.example.demo.model.Movie;
import com.example.demo.model.User;
import com.example.demo.repository.AbstractLinkedCollectionRepository;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.PurchasedMovieRepository;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
public abstract class AbstractLinkedCollectionService<Entity extends AbstractLinkedCollectionItem, DTO> {

    @Autowired
    protected final AbstractLinkedCollectionRepository<Entity> linkedCollectionRepository;
    @Autowired
    protected final UserRepository userRepository;
    @Autowired
    protected final MovieRepository movieRepository;
    @Autowired
    protected final PurchasedMovieRepository purchasedMovieRepository;

    @Transactional(readOnly = true)
    public int getCount(String username) {
        return linkedCollectionRepository.countByUsername(username);
    }

    @Transactional(readOnly = true)
    public Set<Long> getMovieIds(String username) {
        return new LinkedHashSet<>(linkedCollectionRepository.findMovieIdsByUsername(username));
    }

    @Transactional(readOnly = true)
    public List<DTO> getAllOfUser(String username) {
        return linkedCollectionRepository.findAllByUsername(username).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public void add(Long movieId, String username) {
        User user = userRepository.findByUsernameOrThrow(username);
        Movie movie = movieRepository.findByIdOrThrow(movieId);

        if (shouldSkipIfPurchased() && purchasedMovieRepository.existsByMovieIdAndUserUsername(movie.getId(), username)) {
            return;
        }
        if (existsInCollection(movie.getId(), username)) {
            return;
        }

        Entity entity = createEntity(user, movie);
        entity.setUser(user);
        entity.setMovie(movie);
        linkedCollectionRepository.save(entity);
    }

    @Transactional
    public void remove(Long movieId, String username) {
        linkedCollectionRepository.deleteByMovieIdAndUserUsername(movieId, username);
    }

    @Transactional
    public void removeAll(String username) {
        if (linkedCollectionRepository.countByUsername(username) == 0) {
            throw BusinessException.of(getEmptyErrorCode());
        }
        linkedCollectionRepository.deleteAllByUsername(username);
    }

    protected abstract DTO mapToDto(Entity entity);

    protected abstract Entity createEntity(User user, Movie movie);

    protected abstract ErrorCode getEmptyErrorCode();

    protected boolean shouldSkipIfPurchased() {
        return false;
    }

    protected boolean existsInCollection(Long movieId, String username) {
        return linkedCollectionRepository.existsByMovieIdAndUserUsername(movieId, username);
    }
}