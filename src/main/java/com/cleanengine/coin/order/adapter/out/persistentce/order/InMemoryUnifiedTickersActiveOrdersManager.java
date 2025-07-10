package com.cleanengine.coin.order.adapter.out.persistentce.order;

import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.spi.ActiveOrders;
import com.cleanengine.coin.order.domain.spi.ActiveOrdersManager;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InMemoryUnifiedTickersActiveOrdersManager implements ActiveOrdersManager {
    private InMemoryUnifiedTickersActiveOrders activeOrders = new InMemoryUnifiedTickersActiveOrders();

    public void saveOrder(Order order) {
        activeOrders.saveOrder(order);
    }

    public Optional<Order> getOrder(Long orderId) {
        return activeOrders.getOrder(orderId);
    }

    public void removeOrder(Long orderId) {
        activeOrders.removeOrder(orderId);
    }

    @Override
    public ActiveOrders getActiveOrders(String ticker) {
        return activeOrders;
    }

    @Override
    public void removeActiveOrders(String ticker) {
        activeOrders = new InMemoryUnifiedTickersActiveOrders();
    }

    @Override
    public void close() {
        // TODO InMemory상 close시 처리할게 있나?
    }
}
