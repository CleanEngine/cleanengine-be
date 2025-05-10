package com.cleanengine.coin.order.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public abstract class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(name="ticker", length = 10, nullable = false, updatable = false)
    protected String ticker;

    @Column(name="user_id", nullable = false, updatable = false)
    protected Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(name="state", nullable = false)
    protected OrderStatus state;

    // TODO orderSize를 VO로 바꾸어야 함
    @Column(name="order_size")
    protected Double orderSize;

    // TODO price를 VO로 바꾸어야 함
    @Column(name="price", nullable = true)
    protected Double price;

    @Column(name="remaining_size", nullable = true)
    protected Double remainingSize;

    @Column(name="created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    @Column(name="is_marketorder", nullable = false, updatable = false)
    protected Boolean isMarketOrder;

    @Column(name="is_bot", nullable = false, updatable = false)
    protected Boolean isBot;
}
