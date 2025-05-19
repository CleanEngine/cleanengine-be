package com.cleanengine.coin.order.application.queue;

import com.cleanengine.coin.order.domain.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

// TODO infra 계층으로 이동해야
@Slf4j
@Component
public class OrderQueueManagerPool {
    // TODO 원래라면 OrderQueueUnit 각각이 Bean으로 등록되는게 깔끔할 수도 있다.
    private final HashMap<String, OrderQueueManager> orderQueueManagerMap = new HashMap<>();

    @Autowired
    public OrderQueueManagerPool(@Value("${order.tickers}") String[] tickers) {
        for(String ticker : tickers) {
            orderQueueManagerMap.put(ticker, new OrderQueueManager(ticker));
        }
    }

    // TODO 불필요 할지도
    public void addOrder(String ticker, Order order){
        Optional<OrderQueueManager> orderQueueManager = Optional.ofNullable(orderQueueManagerMap.get(ticker));
        orderQueueManager.ifPresent(queueManager -> queueManager.addOrder(order));
    }

    public OrderQueueManager getOrderQueueManager(String ticker){
        Optional<OrderQueueManager> orderQueueManager = Optional.ofNullable(orderQueueManagerMap.get(ticker));
        if(orderQueueManager.isEmpty()){
            log.debug("OrderQueueManager not found. check order.tickers on startup");
            throw new RuntimeException("OrderQueueManager not found. check order.tickers on startup");
        }
        return orderQueueManager.get();
    }
}
