package com.cleanengine.coin.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "buy_orders")
@Table(name="buy_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class BuyOrder {
    @Id @Column(name="buy_order_id", nullable = false) @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="ticker", length = 10, nullable = false, updatable = false)
    private String ticker;

    @Column(name="user_id", nullable = false, updatable = false)
    private Integer userId;

    // TODO size를 VO로 바꾸어야 함
    @Column(name="size")
    private Double size;

    // TODO price를 VO로 바꾸어야 함
    @Column(name="price", nullable = false)
    private Double price;

    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name="is_marketorder", nullable = false, updatable = false)
    private Boolean isMarketOrder;

    @Column(name="is_bot", nullable = false, updatable = false)
    private Boolean isBot;

    public void buy(Double buySize){
        if(buySize > this.size) throw new IllegalArgumentException();
        this.size -= buySize;
    }

    public static BuyOrder create(String ticker, Integer userId, Double size, Double price, LocalDateTime createdAt,
                                  Boolean isMarketOrder, Boolean isBot) {
        BuyOrder buyOrder = new BuyOrder();
        buyOrder.ticker = ticker;
        buyOrder.userId = userId;
        buyOrder.size = size;
        buyOrder.price = price;
        buyOrder.createdAt = createdAt;
        buyOrder.isMarketOrder = isMarketOrder;
        buyOrder.isBot = isBot;
        return buyOrder;
    }
}
