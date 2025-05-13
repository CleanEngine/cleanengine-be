package com.cleanengine.coin.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "wallet")
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Long id;

    @Column(name = "ticker", nullable = false, length = 10)
    private String ticker;

    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @Column(name = "size", nullable = false)
    private Double size;

    @Column(name = "buy_price")
    private Double buyPrice;

    @Column(name = "roi")
    private Double roi; // Return on Investment (수익률)

}
