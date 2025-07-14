package com.cleanengine.coin.user.domain;

import jakarta.persistence.*;
import lombok.*;

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

    @Builder
    private Wallet(String ticker, Integer accountId, Double size, Double buyPrice, Double roi) {
        this.ticker = ticker;
        this.accountId = accountId;
        this.size = size;
        this.buyPrice = buyPrice;
        this.roi = roi;
    }

    public static Wallet of(String ticker, Integer accountId) {
        return Wallet.builder()
                .ticker(ticker)
                .accountId(accountId)
                .size(0.0)
                .buyPrice(0.0)
                .roi(0.0)
                .build();
    }

    public static Wallet of(String ticker, Integer accountId, Double size) {
        return Wallet.builder()
                .ticker(ticker)
                .accountId(accountId)
                .size(size)
                .buyPrice(0.0)
                .roi(0.0)
                .build();
    }

    public static Wallet generateEmptyWallet(String ticker, Integer accountId){
        Wallet wallet = new Wallet();
        wallet.setTicker(ticker);
        wallet.setAccountId(accountId);
        wallet.setSize(0.0);
        wallet.setBuyPrice(0.0);
        wallet.setRoi(0.0);
        return wallet;
    }

    public void decreaseSize(Double orderSize) {
        if(orderSize <= 0){
            throw new IllegalArgumentException("orderSize must be greater than zero.");
        }

        if(this.getSize() < orderSize){
            throw new IllegalArgumentException("Cannot decrease size. Available size: " + this.getSize() + ", requested: " + orderSize);
        }

        this.size = this.getSize() - orderSize;
    }

    public void increaseSize(Double orderSize) {
        if(orderSize <= 0){
            throw new IllegalArgumentException("orderSize must be greater than zero.");
        }

        this.size = this.getSize() + orderSize;
    }

    public void reset() {
        this.size = 0.0;
        this.buyPrice = 0.0;
        this.roi = 0.0;
    }

}
