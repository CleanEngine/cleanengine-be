package com.cleanengine.coin.order.application.strategy;

import com.cleanengine.coin.order.application.OrderCommand;
import com.cleanengine.coin.order.application.OrderInfo;
import com.cleanengine.coin.order.application.queue.OrderQueueManagerPool;
import com.cleanengine.coin.order.domain.Order;

public abstract class CreateOrderStrategy<T extends Order, S extends OrderInfo<?>> {
    public S processCreatingOrder(OrderCommand.CreateOrder createOrderCommand){
        T order = createOrder(createOrderCommand);
        saveOrder(order);
        keepHoldings(order);
        orderQueueManagerPool().addOrder(order.getTicker(), order);
        return extractOrderInfo(order);
    }
    protected abstract T createOrder(OrderCommand.CreateOrder createOrderCommand);
    protected abstract void saveOrder(T order);
    protected abstract S extractOrderInfo(T order);
    public abstract boolean supports(Boolean isBuyOrder);
    protected abstract void keepHoldings(T order) throws RuntimeException;
    protected abstract OrderQueueManagerPool orderQueueManagerPool();
}
