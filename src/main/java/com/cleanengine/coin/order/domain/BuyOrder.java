package com.cleanengine.coin.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "buy_orders")
@Table(name="buy_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class BuyOrder {
    @Id @Column(name="buy_order_id", nullable = false) @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="ticker", length = 10, nullable = false, updatable = false)
    private String ticker;

    @Column(name="user_id", nullable = false, updatable = false)
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(name="state", nullable = false)
    private OrderStatus state;

    @Column(name="locked_deposit", nullable = false, updatable = false)
    private Double lockedDeposit;

    // TODO orderSize를 VO로 바꾸어야 함
    @Column(name="order_size")
    private Double orderSize;

    // TODO price를 VO로 바꾸어야 함
    @Column(name="price", nullable = true)
    private Double price;

    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name="is_marketorder", nullable = false, updatable = false)
    private Boolean isMarketOrder;

    @Column(name="remaining_deposit", nullable = false)
    private Double remainingDeposit;

    @Column(name="remaining_size", nullable = true)
    private Double remainingSize;

    @Column(name="is_bot", nullable = false, updatable = false)
    private Boolean isBot;

    public static BuyOrder createMarketBuyOrder(String ticker, Integer userId, Double price,
                                                LocalDateTime createdAt, Boolean isBot) {
      BuyOrder buyOrder = new BuyOrder();
      buyOrder.ticker = ticker;
      buyOrder.userId = userId;
      buyOrder.state = OrderStatus.WAIT;
      buyOrder.lockedDeposit = price;
      buyOrder.orderSize = null;
      buyOrder.price = null;
      buyOrder.createdAt = createdAt;
      buyOrder.isMarketOrder = true;
      buyOrder.remainingDeposit = buyOrder.lockedDeposit;
      buyOrder.remainingSize = null;
      buyOrder.isBot = isBot;
      return buyOrder;
  }
  public static BuyOrder createLimitBuyOrder(String ticker, Integer userId, Double orderSize,
                                  Double price, LocalDateTime createdAt, Boolean isBot) {
      BuyOrder buyOrder = new BuyOrder();
      buyOrder.ticker = ticker;
      buyOrder.userId = userId;
      buyOrder.state = OrderStatus.WAIT;
      buyOrder.lockedDeposit = orderSize * price;
      buyOrder.orderSize = orderSize;
      buyOrder.price = price;
      buyOrder.createdAt = createdAt;
      buyOrder.isMarketOrder = false;
      buyOrder.remainingDeposit = buyOrder.lockedDeposit;
      buyOrder.remainingSize = orderSize;
      buyOrder.isBot = isBot;
      return buyOrder;
  }
}
