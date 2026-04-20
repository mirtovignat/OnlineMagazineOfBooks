package com.example.demo.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "movies")
public class Movie extends AbstractEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "duration_seconds")
    private Duration duration;

    @Column(name = "genre")
    private String genre;

    @Column(name = "rating", precision = 4, scale = 2)
    private BigDecimal rating = BigDecimal.valueOf(0);

    @Column(name = "rating_count")
    private Integer ratingsCount = 0;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "director", columnDefinition = "text")
    private String director;

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    private Set<CartItem> cartItems = new LinkedHashSet<>();

    public void addCartItem(CartItem cartItem) {
        this.cartItems.add(cartItem);
    }

    public void removeCartItem(CartItem cartItem) {
        this.cartItems.remove(cartItem);
    }

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    private Set<PurchasedMovie> purchases = new LinkedHashSet<>();

    public void addPurchase(PurchasedMovie purchasedMovie) {
        this.purchases.add(purchasedMovie);
    }

    public void removePurchase(PurchasedMovie purchasedMovie) {
        this.purchases.remove(purchasedMovie);
    }

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    private Set<FavouriteMovie> favourites = new LinkedHashSet<>();

    public void addFavourite(FavouriteMovie favouriteMovie) {
        this.favourites.add(favouriteMovie);
    }

    public void removeFavourite(FavouriteMovie favouriteMovie) {
        this.favourites.remove(favouriteMovie);
    }

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    private Set<RatedMovie> ratings = new LinkedHashSet<>();

    public void addRated(RatedMovie ratedMovie) {
        this.ratings.add(ratedMovie);
    }

    public void removeRated(RatedMovie ratedMovie) {
        this.ratings.remove(ratedMovie);
    }

    public void updateRating() {
        BigDecimal sum = BigDecimal.ZERO;
        for (RatedMovie ratedMovie : ratings) {
            sum = sum.add(ratedMovie.getRatingValue());
        }
        BigDecimal count = BigDecimal.valueOf(ratings.size());

        if (!count.equals(BigDecimal.ZERO)) {
            this.rating = sum.divide(count, 1, RoundingMode.HALF_UP);
        }
    }
}

