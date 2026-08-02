package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
@SQLRestriction("deleted = false")
public class User extends AbstractEntity {

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

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
    private String currencyCode = "RUB";

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<FavouriteMovie> favourites = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<RatedMovie> ratings = new LinkedHashSet<>();



    public void addMoney(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void spendMoney(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return;
        }

        if (balance.compareTo(amount) < 0) {
            return;
        }

        balance = balance.subtract(amount);
    }
}


