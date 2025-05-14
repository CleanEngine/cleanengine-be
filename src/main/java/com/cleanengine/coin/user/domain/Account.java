package com.cleanengine.coin.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "account")
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "cash", nullable = false)
    private Double cash;

    // Cash 증가
    public Account increaseCash(Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Increase amount must be greater than zero.");
        }
        this.cash += amount;
        return this;
    }

    // Cash 감소
    public Account decreaseCash(Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Decrease amount must be greater than zero.");
        }
        if (this.cash < amount) {
            throw new IllegalArgumentException("Cannot decrease cash. Available cash: " + this.cash + ", requested: " + amount);
        }
        this.cash -= amount;
        return this;
    }

}
