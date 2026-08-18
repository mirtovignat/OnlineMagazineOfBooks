package com.example.demo.service.catalog;

import com.example.demo.dto.catalog.RatedMovieForOwnerViewDTO;
import com.example.demo.mapper.RatedMapper;
import com.example.demo.model.RatedMovie;
import com.example.demo.repository.RatedMovieRepository;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class RatedCatalogService extends AbstractCatalogService
        <RatedMovie, RatedMovieForOwnerViewDTO> {

    private final RatedMapper ratedMapper;

    public RatedCatalogService(RatedMovieRepository ratedMovieRepository,
                               RatedMapper ratedMapper) {
        super(ratedMovieRepository);
        this.ratedMapper = ratedMapper;
    }

    @Override
    protected Function<RatedMovie, RatedMovieForOwnerViewDTO> getMapper() {
        return ratedMapper::toOwnerView;
    }
}
