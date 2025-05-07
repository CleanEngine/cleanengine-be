package com.cleanengine.coin.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "sell_orders")
@Table(name="sell_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SellOrder {
    @Id @Column(name="sell_order_id", nullable = false) @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="ticker", length = 10, nullable = false, updatable = false)
    private String ticker;

    @Column(name="user_id", nullable = false, updatable = false)
    private Long userId;

    // TODO size를 VO로 바꾸어야 함
    @Column(name="size", nullable = false)
    private Double size;

    // TODO price를 VO로 바꾸어야 함
    @Column(name="price")
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

    public static SellOrder create(String ticker, Long userId, Double size, Double price, LocalDateTime createdAt,
                                   Boolean isMarketOrder, Boolean isBot) {
        SellOrder sellOrder = new SellOrder();
        sellOrder.ticker = ticker;
        sellOrder.userId = userId;
        sellOrder.size = size;
        sellOrder.price = price;
        sellOrder.createdAt = createdAt;
        sellOrder.isMarketOrder = isMarketOrder;
        sellOrder.isBot = isBot;
        return sellOrder;
    }
}
