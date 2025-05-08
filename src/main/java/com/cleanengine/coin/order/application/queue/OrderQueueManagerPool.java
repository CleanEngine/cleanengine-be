package com.cleanengine.coin.order.application.queue;

import com.cleanengine.coin.order.domain.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;

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
        orderQueueManagerMap.get(ticker).addOrder(order);
    }

    public OrderQueueManager getOrderQueueManager(String ticker){
        return orderQueueManagerMap.get(ticker);
    }
}
