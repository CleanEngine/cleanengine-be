package com.cleanengine.coin.order.adapter.out.persistentce.order.queue;

import com.cleanengine.coin.order.domain.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

// TODO infra 계층으로 이동해야
@Slf4j
@Component
public class OrderQueueManagerPool {
    // TODO 원래라면 OrderQueueUnit 각각이 Bean으로 등록되는게 깔끔할 수도 있다.
    private final HashMap<String, OrderQueueManager> orderQueueManagerMap = new HashMap<>();

    // TODO 불필요 할지도
    public void addOrder(String ticker, Order order){
        OrderQueueManager orderQueueManager = getOrderQueueManager(ticker);
        orderQueueManager.addOrder(order);
    }

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
