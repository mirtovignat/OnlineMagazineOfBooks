package com.example.demo.service.catalog;

import com.example.demo.dto.joined_to_user.LibrarianMovieForOwnerViewDTO;
import com.example.demo.mapper.PurchasedMapper;
import com.example.demo.model.PurchasedMovie;
import com.example.demo.repository.PurchasedMovieRepository;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class LibraryService extends AbstractCatalogService<PurchasedMovie, LibrarianMovieForOwnerViewDTO> {

    private final PurchasedMapper purchasedMapper;

    protected LibraryService(PurchasedMovieRepository purchasedMovieRepository,
                             PurchasedMapper purchasedMapper) {
        super(purchasedMovieRepository);
        this.purchasedMapper = purchasedMapper;
    }

    @Override
    protected Function<PurchasedMovie, LibrarianMovieForOwnerViewDTO> getMapper() {
        return purchasedMapper::toOwnerViewFromLibrarian;
    }
}