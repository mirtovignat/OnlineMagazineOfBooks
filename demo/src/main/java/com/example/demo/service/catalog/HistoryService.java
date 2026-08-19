package com.example.demo.service.catalog;

import com.example.demo.dto.catalog.HistoricalMovieForOwnerViewDTO;
import com.example.demo.mapper.entity.PurchasedMapper;
import com.example.demo.model.entity.PurchasedMovie;
import com.example.demo.repository.entity.PurchasedMovieRepository;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class HistoryService extends AbstractCatalogService<PurchasedMovie,
        HistoricalMovieForOwnerViewDTO> {

    private final PurchasedMapper purchasedMapper;

    protected HistoryService(PurchasedMovieRepository purchasedMovieRepository, PurchasedMapper purchasedMapper) {
        super(purchasedMovieRepository);
        this.purchasedMapper = purchasedMapper;
    }

    @Override
    protected Function<PurchasedMovie, HistoricalMovieForOwnerViewDTO> getMapper() {
        return purchasedMapper::toOwnerViewFromHistorical;
    }
}