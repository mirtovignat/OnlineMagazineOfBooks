package com.example.demo.repository.specification;

import com.example.demo.filter.MovieFilter;
import com.example.demo.model.Movie;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public final class MovieSpecifications {

    private MovieSpecifications() {
    }

    public static Specification<Movie> byFilter(MovieFilter movieFilter) {
        if (movieFilter == null) {
            return Specification.unrestricted();
        }
        return Stream.of(
                        movieFilter.priceFrom() != null ? greaterThanOrEqualTo("price", movieFilter.priceFrom()) : null,
                        movieFilter.priceTo() != null ? lessThanOrEqualTo("price", movieFilter.priceTo()) : null,
                        movieFilter.releaseDateFrom() != null ? greaterThanOrEqualTo("releaseDate", movieFilter.releaseDateFrom()) : null,
                        movieFilter.releaseDateTo() != null ? lessThanOrEqualTo("releaseDate", movieFilter.releaseDateTo()) : null,
                        movieFilter.durationFrom() != null ? greaterThanOrEqualTo("duration", movieFilter.durationFrom()) : null,
                        movieFilter.durationTo() != null ? lessThanOrEqualTo("duration", movieFilter.durationTo()) : null,
                        movieFilter.ratingFrom() != null ? greaterThanOrEqualTo("rating", movieFilter.ratingFrom()) : null,
                        movieFilter.ratingTo() != null ? lessThanOrEqualTo("rating", movieFilter.ratingTo()) : null,
                        (movieFilter.genres() != null && !movieFilter.genres().isEmpty()) ? isInCollection("genre", movieFilter.genres()) : null,
                        (movieFilter.directors() != null && !movieFilter.directors().isEmpty()) ? isInCollection("director", movieFilter.directors()) : null
                )
                .filter(Objects::nonNull)
                .reduce(Specification.unrestricted(), Specification::and);
    }

    public static Specification<Movie> fetchPurchasesAndReviews() {
        return (root, query, criteriaBuilder) -> {
            assert query != null;
            if (Long.class.equals(query.getResultType())) {
                return null;
            }
            root.fetch("purchases", JoinType.LEFT);
            query.distinct(true);
            return null;
        };
    }

    private static <Y extends Comparable<? super Y>> Specification<Movie> greaterThanOrEqualTo(String fieldName,
                                                                                               Y value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get(fieldName), value);
    }

    private static <Y extends Comparable<? super Y>> Specification<Movie> lessThanOrEqualTo(String fieldName,
                                                                                            Y value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get(fieldName), value);
    }

    private static <Y> Specification<Movie> isInCollection(String fieldName,
                                                           Collection<Y> collection) {
        return (root, query, criteriaBuilder) -> root.get(fieldName).in(collection);
    }
}