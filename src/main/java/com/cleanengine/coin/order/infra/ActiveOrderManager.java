package com.cleanengine.coin.order.infra;

import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.SellOrder;
import lombok.Getter;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveOrderManager {
    @Getter
    private final String ticker;
    private final ConcurrentHashMap<Long, BuyOrder> activeBuyOrders = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, SellOrder> activeSellOrders = new ConcurrentHashMap<>();

    public ActiveOrderManager(String ticker) {
        this.ticker = ticker;
    }

    public void saveOrder(Order order) {
        if(order.getIsMarketOrder()){
            return;
        }
        if(order instanceof BuyOrder){
            activeBuyOrders.put(order.getId(), (BuyOrder) order);
        } else {
            activeSellOrders.put(order.getId(), (SellOrder) order);
        }
    }

    public Optional<Order> getOrder(Long orderId, boolean isBuyOrder) {
        if(isBuyOrder) {
            return Optional.ofNullable(activeBuyOrders.get(orderId));
        } else {
            return Optional.ofNullable(activeSellOrders.get(orderId));
        }
    }

    public void removeOrder(Long orderId, boolean isBuyOrder) {
        if(isBuyOrder) {
            activeBuyOrders.remove(orderId);
        } else {
            activeSellOrders.remove(orderId);
        }
    }
}
