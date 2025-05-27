package com.cleanengine.coin.order.application;

import com.cleanengine.coin.common.annotation.WorkingServerProfile;
import com.cleanengine.coin.order.application.queue.OrderQueueManagerPool;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.order.infra.query.BuyOrderQueryRepository;
import com.cleanengine.coin.order.infra.query.SellOrderQueryRepository;
import com.cleanengine.coin.orderbook.application.service.UpdateOrderBookUsecase;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@WorkingServerProfile
@org.springframework.core.annotation.Order(3)
@RequiredArgsConstructor
public class OrderRestoreService implements ApplicationRunner {

    private final OrderQueueManagerPool orderQueueManagerPool;
    private final UpdateOrderBookUsecase updateOrderBookUsecase;
    private final BuyOrderQueryRepository buyOrderQueryRepository;
    private final SellOrderQueryRepository sellOrderQueryRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<BuyOrder> buyOrders = buyOrderQueryRepository.findIncompletedBuyOrders();
        buyOrders.forEach(this::restoreOrder);
        List<SellOrder> sellOrders = sellOrderQueryRepository.findIncompletedSellOrders();
        sellOrders.forEach(this::restoreOrder);
    }

    protected void restoreOrder(Order order){
        orderQueueManagerPool.addOrder(order.getTicker(), order);
        updateOrderBookUsecase.updateOrderBookOnNewOrder(order);
    }
}
