package com.cleanengine.coin.order.adapter.out.persistentce.order;

import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.spi.ActiveOrders;
import com.cleanengine.coin.order.domain.spi.ActiveOrdersManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryActiveOrdersManager implements ActiveOrdersManager {
    private final HashMap<String, InMemoryActiveOrders> activeOrdersMap = new HashMap<>();

    public void saveOrder(String ticker, Order order) {
        ActiveOrders activeOrders = getActiveOrders(ticker);
        activeOrders.saveOrder(order);
    }

    public Optional<Order> getOrder(String ticker, Long orderId, boolean isBuyOrder) {
        ActiveOrders activeOrders = getActiveOrders(ticker);
        return activeOrders.getOrder(orderId,isBuyOrder);
    }

    public void removeOrder(String ticker, Long orderId, boolean isBuyOrder) {
        ActiveOrders activeOrders = getActiveOrders(ticker);
        activeOrders.removeOrder(orderId, isBuyOrder);
    }

    protected synchronized void addActiveOrderManager(String ticker) {
        if(!activeOrdersMap.containsKey(ticker)){
            activeOrdersMap.put(ticker, new InMemoryActiveOrders(ticker));
        }
    }

    @Override
    public ActiveOrders getActiveOrders(String ticker) {
        if(!activeOrdersMap.containsKey(ticker)){
            addActiveOrderManager(ticker);
        }

        Optional<InMemoryActiveOrders> activeOrderManager = Optional.ofNullable(activeOrdersMap.get(ticker));
        if(activeOrderManager.isEmpty()){
            log.debug("ActiveOrderManager not found with " + ticker);
            throw new RuntimeException("ActiveOrderManager not found with " + ticker);
        }
        return activeOrderManager.get();
    }

    @Override
    public void removeActiveOrders(String ticker) {
        activeOrdersMap.remove(ticker);
    }

    @Override
    public void close() {
        // TODO InMemory상 close시 처리할게 있나?
    }
}
