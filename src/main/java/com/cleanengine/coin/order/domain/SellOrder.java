package com.cleanengine.coin.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "sell_orders")
@Table(name="sell_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class SellOrder {
    @Id @Column(name="sell_order_id", nullable = false) @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="ticker", length = 10, nullable = false, updatable = false)
    private String ticker;

    @Column(name="user_id", nullable = false, updatable = false)
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(name="state", nullable = false)
    private OrderStatus state;

    // TODO size를 VO로 바꾸어야 함
    @Column(name="order_size", nullable = false)
    private Double orderSize;

    // TODO price를 VO로 바꾸어야 함
    @Column(name="price", nullable = true)
    private Double price;

    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name="is_marketorder", nullable = false, updatable = false)
    private Boolean isMarketOrder;

    @Column(name="remaining_size", nullable = true)
    private Double remainingSize;

    @Column(name="is_bot", nullable = false, updatable = false)
    private Boolean isBot;

    public static SellOrder createMarketSellOrder(String ticker, Integer userId, Double orderSize,
                                                  LocalDateTime createdAt, Boolean isBot) {
        SellOrder sellOrder = new SellOrder();
        sellOrder.ticker = ticker;
        sellOrder.userId = userId;
        sellOrder.state = OrderStatus.WAIT;
        sellOrder.orderSize = orderSize;
        sellOrder.price = null;
        sellOrder.createdAt = createdAt;
        sellOrder.isMarketOrder = true;
        sellOrder.remainingSize = orderSize;
        sellOrder.isBot = isBot;
        return sellOrder;
    }

    public static SellOrder createLimitSellOrder(String ticker, Integer userId, Double orderSize,
                                                 Double price, LocalDateTime createdAt, Boolean isBot) {
        SellOrder sellOrder = new SellOrder();
        sellOrder.ticker = ticker;
        sellOrder.userId = userId;
        sellOrder.state = OrderStatus.WAIT;
        sellOrder.orderSize = orderSize;
        sellOrder.price = price;
        sellOrder.createdAt = createdAt;
        sellOrder.isMarketOrder = false;
        sellOrder.remainingSize = orderSize;
        sellOrder.isBot = isBot;
        return sellOrder;
    }
}
