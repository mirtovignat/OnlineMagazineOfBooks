package com.example.demo.mapper;

import com.example.demo.model.FavouriteMovie;
import com.example.demo.model.Movie;
import com.example.demo.model.PurchasedMovie;
import com.example.demo.model.RatedMovie;
import lombok.AllArgsConstructor;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class MapperUtils {

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Named("toFullName")
    public String toFullName(String surname,
                             String name,
                             String patronymic) {
        if (surname == null) {
            return null;
        }
        if (name == null) {
            return null;
        }
        if (patronymic == null) {
            return null;
        }
        return surname + " " + name + " " + patronymic;

    }

    @Named("rawToEncoded")
    public String encodePassword(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        return passwordEncoder.encode(rawPassword);
    }

    @Named("moviesToIds")
    public List<Long> moviesToIds(Collection<? extends Movie> movies) {
        if (movies == null) {
            return Collections.emptyList();
        }
        return movies.stream()
                .filter(Objects::nonNull)
                .map(Movie::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Named("purchasedMoviesToMoviesIds")
    public List<Long> purchasedMoviesToMoviesIds(Collection<PurchasedMovie> purchases) {
        if (purchases == null) {
            return Collections.emptyList();
        }
        return purchases.stream()
                .filter(Objects::nonNull)
                .map(p -> {
                    var m = p.getMovie();
                    return m == null ? null : m.getId();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Named("ratedMoviesToMoviesIds")
    public List<Long> ratedMoviesToMoviesIds(Collection<RatedMovie> ratings) {
        if (ratings == null) {
            return Collections.emptyList();
        }
        return ratings.stream()
                .filter(Objects::nonNull)
                .map(r -> {
                    var m = r.getMovie();
                    return m == null ? null : m.getId();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Named("favouriteMoviesToMoviesIds")
    public List<Long> favouriteMoviesToMoviesIds(Collection<FavouriteMovie> favourites) {
        if (favourites == null) {
            return Collections.emptyList();
        }
        return favourites.stream()
                .filter(Objects::nonNull)
                .map(favouriteMovie -> {
                    Movie movie = favouriteMovie.getMovie();
                    return movie == null ? null : movie.getId();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Named("sizeToLong")
    public Long sizeToLong(Collection<?> collection) {
        return collection == null ? 0L : (long) collection.size();
    }

    @Named("nullableNumberToLong")
    public Long nullableNumberToLong(Number number) {
        return number == null ? 0L : number.longValue();
    }

    @Named("yearFromLocalDate")
    public Integer yearFromLocalDate(LocalDate localDate) {
        return localDate == null ? null : localDate.getYear();
    }

    @Named("movieToId")
    public Long movieToId(Movie movie) {
        return movie == null ? null : movie.getId();
    }

    @Named("fromAmountToBalance")
    public BigDecimal fromAmountToBalance(Double amount) {
        return BigDecimal.valueOf(amount);
    }
}
