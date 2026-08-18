package com.example.demo.service.catalog;

import com.example.demo.model.AbstractCatalogItem;
import com.example.demo.repository.AbstractCatalogRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;

public abstract class AbstractCatalogService<CatalogItem extends AbstractCatalogItem, DTO> {

    protected final AbstractCatalogRepository<CatalogItem> catalogRepository;

    protected AbstractCatalogService(AbstractCatalogRepository<CatalogItem> catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    protected abstract Function<CatalogItem, DTO> getMapper();

    @Transactional(readOnly = true)
    public List<DTO> getCatalog(Long id) {
        return getById(id, getMapper());
    }

    @Transactional(readOnly = true)
    public List<DTO> getById(Long id, Function<CatalogItem, DTO> mapper) {
        return catalogRepository.findAllByUserId(id)
                .stream()
                .map(mapper)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean existsByMovieIdAndUserId(Long movieId, Long userId) {
        return catalogRepository.existsByMovieIdAndUserId(movieId, userId);
    }
}