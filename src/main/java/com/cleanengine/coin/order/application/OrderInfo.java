package com.cleanengine.coin.order.application;

import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.OrderStatus;
import com.cleanengine.coin.order.domain.SellOrder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public abstract class OrderInfo<T extends Order>{
    protected final Long id;
    protected final String ticker;
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
    }

    @Getter
    public static final class SellOrderInfo extends OrderInfo<SellOrder> {
        public SellOrderInfo(SellOrder order) {
            super(order, false);
        }
    }
}