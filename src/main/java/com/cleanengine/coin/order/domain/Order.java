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

    @Column(name="created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    @Column(name="is_marketorder", nullable = false, updatable = false)
    protected Boolean isMarketOrder;

    @Column(name="is_bot", nullable = false, updatable = false)
    protected Boolean isBot;
}
