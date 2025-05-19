package com.cleanengine.coin.order.application.strategy;

import com.cleanengine.coin.order.application.OrderCommand;
import com.cleanengine.coin.order.application.OrderInfo;
import com.cleanengine.coin.order.application.queue.OrderQueueManagerPool;
import com.cleanengine.coin.order.domain.Order;

public abstract class CreateOrderStrategy<T extends Order, S extends OrderInfo<?>> {

    public T processCreatingOrder(OrderCommand.CreateOrder createOrderCommand){
        T order = createOrder(createOrderCommand);
        saveOrder(order);
        createWallet(order.getUserId(), order.getTicker());
        keepHoldings(order);
        updateOrderBook(order);
        return order;
    }

    public abstract boolean supports(Boolean isBuyOrder);

    protected abstract T createOrder(OrderCommand.CreateOrder createOrderCommand);
    protected abstract void saveOrder(T order);
    protected abstract void createWallet(Integer userId, String ticker);
    protected abstract void keepHoldings(T order) throws RuntimeException;
    protected abstract void updateOrderBook(T order);
    public abstract S extractOrderInfo(Order order);
}
