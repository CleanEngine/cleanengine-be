package com.cleanengine.coin.order.adapter.out.persistentce.order.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Deprecated
@Slf4j
@Component
public class OrderQueueManagerPool {
    private final HashMap<String, OrderQueueManager> orderQueueManagerMap = new HashMap<>();

    public OrderQueueManager getOrderQueueManager(String ticker){
        if(!orderQueueManagerMap.containsKey(ticker)){
            addOrderQueueManager(ticker);
        }

        Optional<OrderQueueManager> orderQueueManagerOpt = Optional.ofNullable(orderQueueManagerMap.get(ticker));
        if(orderQueueManagerOpt.isEmpty()){
            log.debug("OrderQueueManager not found. with " + ticker);
            throw new RuntimeException("OrderQueueManager not found with " + ticker);
        }
        return orderQueueManagerOpt.get();
    }

    protected synchronized void addOrderQueueManager(String ticker){
        if(!orderQueueManagerMap.containsKey(ticker)){
            orderQueueManagerMap.put(ticker, new OrderQueueManager(ticker));
        }
    }
}
