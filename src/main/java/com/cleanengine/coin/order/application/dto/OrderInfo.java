package com.cleanengine.coin.order.application.dto;

import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.OrderStatus;
import com.cleanengine.coin.order.domain.SellOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class OrderInfo<T extends Order>{
    protected Long id;
    protected String ticker;
    protected OrderStatus state;
    protected Integer userId;
    protected Boolean isBuyOrder;
    protected Boolean isMarketOrder;
    protected Double orderSize;
    protected Double price;
    protected LocalDateTime createdAt;

    protected OrderInfo(T order, Boolean isBuyOrder) {
        this.id = order.getId();
        this.ticker = order.getTicker();
        this.state = order.getState();
        this.userId = order.getUserId();
        this.isBuyOrder = isBuyOrder;
        this.isMarketOrder = order.getIsMarketOrder();
        this.orderSize = order.getOrderSize();
        this.price = order.getPrice();
        this.createdAt = order.getCreatedAt();
    }

    @Getter
    public static final class BuyOrderInfo extends OrderInfo<BuyOrder> {
        private Double lockedDeposit;
        private Double remainingDeposit;

        public BuyOrderInfo(BuyOrder order) {
            super(order, true);
            this.lockedDeposit = order.getLockedDeposit();
            this.remainingDeposit = order.getRemainingDeposit();
        }

        public BuyOrderInfo(Long id, String ticker, OrderStatus state, Integer userId, Boolean isBuyOrder,
                Boolean isMarketOrder, Double orderSize, Double price, LocalDateTime createdAt, Double lockedDeposit,
                               Double remainingDeposit) {
            super(id, ticker, state, userId, isBuyOrder, isMarketOrder, orderSize, price, createdAt);
            this.lockedDeposit = lockedDeposit;
            this.remainingDeposit = remainingDeposit;
        }
    }

    @Getter
    public static final class SellOrderInfo extends OrderInfo<SellOrder> {
        public SellOrderInfo(SellOrder order) {
            super(order, false);
        }

        public SellOrderInfo(Long id, String ticker, OrderStatus state, Integer userId, Boolean isBuyOrder,
                Boolean isMarketOrder, Double orderSize, Double price, LocalDateTime createdAt) {
            super(id, ticker, state, userId, isBuyOrder, isMarketOrder, orderSize, price, createdAt);
        }
    }
}