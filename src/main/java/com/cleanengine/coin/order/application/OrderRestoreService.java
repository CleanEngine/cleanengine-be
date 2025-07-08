package com.cleanengine.coin.order.application;

import com.cleanengine.coin.common.annotation.WorkingServerProfile;
import com.cleanengine.coin.order.adapter.out.persistentce.order.query.BuyOrderQueryRepository;
import com.cleanengine.coin.order.adapter.out.persistentce.order.query.SellOrderQueryRepository;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.order.domain.spi.ActiveOrdersManager;
import com.cleanengine.coin.order.domain.spi.WaitingOrders;
import com.cleanengine.coin.order.domain.spi.WaitingOrdersManager;
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

    private final WaitingOrdersManager waitingOrdersManager;
    private final UpdateOrderBookUsecase updateOrderBookUsecase;
    private final BuyOrderQueryRepository buyOrderQueryRepository;
    private final SellOrderQueryRepository sellOrderQueryRepository;
    private final ActiveOrdersManager activeOrdersManager;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<BuyOrder> buyOrders = buyOrderQueryRepository.findIncompletedBuyOrders();
        buyOrders.forEach(this::restoreOrder);
        List<SellOrder> sellOrders = sellOrderQueryRepository.findIncompletedSellOrders();
        sellOrders.forEach(this::restoreOrder);
    }

    protected void restoreOrder(Order order){
        WaitingOrders waitingOrders = waitingOrdersManager.getWaitingOrders(order.getTicker());
        waitingOrders.addOrder(order);

        activeOrdersManager.getActiveOrders(order.getTicker()).saveOrder(order);

        updateOrderBookUsecase.updateOrderBookOnRestored(order);
    }
}
