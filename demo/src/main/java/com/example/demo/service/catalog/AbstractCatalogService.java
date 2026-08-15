package com.example.demo.service.catalog;

import com.example.demo.model.AbstractCatalogItem;
import com.example.demo.repository.AbstractCatalogRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;

public abstract class AbstractCatalogService<CatalogItem extends
        AbstractCatalogItem, DTO> {

    protected final AbstractCatalogRepository<CatalogItem> catalogRepository;

    protected AbstractCatalogService(AbstractCatalogRepository<
            CatalogItem> catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    protected abstract Function<CatalogItem, DTO> getMapper();

    @Transactional(readOnly = true)
    public List<DTO> getCatalog(String username) {
        return getByUsername(username, getMapper());
    }

    @Transactional(readOnly = true)
    public List<DTO> getByUsername(String username,
                                   Function<CatalogItem, DTO> mapper) {
        return catalogRepository.findAllByUsername(username)
                .stream()
                .map(mapper)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean existsByMovieIdAndUsername(Long movieId, String username) {
        return catalogRepository.existsByMovieIdAndUserUsername(movieId, username);
    }
}