package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends AbstractEntity {
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "phone", unique = true)
    private String phone;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<PurchasedMovie> purchases = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<CartItem> cartItems = new LinkedHashSet<>();

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "currency_code", nullable = false)
    private final String currencyCode = "RUB";

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<FavouriteMovie> favourites = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<RatedMovie> ratings = new LinkedHashSet<>();

    public void addCartItem(CartItem cartItem) {
        this.cartItems.add(cartItem);
    }

    public void removeCartItem(CartItem cartItem) {
        this.cartItems.remove(cartItem);
    }

    public void addPurchase(PurchasedMovie purchasedMovie) {
        this.purchases.add(purchasedMovie);
    }

    public void removePurchase(PurchasedMovie purchasedMovie) {
        this.purchases.remove(purchasedMovie);
    }

    public void addFavourite(FavouriteMovie favouriteMovie) {
        this.favourites.add(favouriteMovie);
    }

    public void removeFavourite(FavouriteMovie favouriteMovie) {
        this.favourites.remove(favouriteMovie);
    }

    public void addRated(RatedMovie ratedMovie) {
        this.ratings.add(ratedMovie);
    }

    public void removeRated(RatedMovie ratedMovie) {
        this.ratings.remove(ratedMovie);
    }

    public void addMoney(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }

    public boolean spendMoney(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return false;
        }

        if (balance.compareTo(amount) < 0) {
            return false;
        }

        balance = balance.subtract(amount);
        return true;
    }
}


