package com.cleanengine.coin.order.infra;

import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.SellOrder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Optional;

public class ActiveOrderManager {
    @Getter
    private final String ticker;
    private final HashMap<Long, BuyOrder> activeBuyOrders = new HashMap<>();
    private final HashMap<Long, SellOrder> activeSellOrders = new HashMap<>();

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
