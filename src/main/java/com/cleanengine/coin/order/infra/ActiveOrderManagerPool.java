package com.cleanengine.coin.order.infra;

import com.cleanengine.coin.order.domain.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Component
public class ActiveOrderManagerPool {
    private final HashMap<String, ActiveOrderManager> activeOrderManagerPool = new HashMap<>();

    @Autowired
    public ActiveOrderManagerPool(@Value("${order.tickers}") String[] tickers) {
        for (String ticker : tickers) {
            activeOrderManagerPool.put(ticker, new ActiveOrderManager(ticker));
        }
    }

    public void saveOrder(String ticker, Order order) {
        ActiveOrderManager activeOrderManager = getActiveOrderManager(ticker);
        activeOrderManager.saveOrder(order);
    }

    public Optional<Order> getOrder(String ticker, Long orderId, boolean isBuyOrder) {
        ActiveOrderManager activeOrderManager = getActiveOrderManager(ticker);
        return activeOrderManager.getOrder(orderId,isBuyOrder);
    }

    public void removeOrder(String ticker, Long orderId, boolean isBuyOrder) {
        ActiveOrderManager activeOrderManager = getActiveOrderManager(ticker);
        activeOrderManager.removeOrder(orderId, isBuyOrder);
    }

    private ActiveOrderManager getActiveOrderManager(String ticker) {
        Optional<ActiveOrderManager> activeOrderManager = Optional.ofNullable(activeOrderManagerPool.get(ticker));
        if(activeOrderManager.isEmpty()){
            log.debug("ActiveOrderManager not found. check order.tickers on startup");
            throw new RuntimeException("ActiveOrderManager not found. check order.tickers on startup");
        }
        return activeOrderManager.get();
    }
}
