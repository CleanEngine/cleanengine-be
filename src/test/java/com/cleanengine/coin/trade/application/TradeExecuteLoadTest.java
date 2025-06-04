package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.common.domain.port.PriorityQueueStore;
import com.cleanengine.coin.order.application.OrderService;
import com.cleanengine.coin.order.application.dto.OrderCommand;
import com.cleanengine.coin.order.application.dto.OrderInfo;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.OrderType;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.order.domain.spi.WaitingOrders;
import com.cleanengine.coin.order.domain.spi.WaitingOrdersManager;
import com.cleanengine.coin.trade.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
class TradeExecuteLoadTest {

    @Autowired
    TradeBatchProcessor tradeBatchProcessor;

    @Autowired
    ApplicationArguments applicationArguments;

    @Autowired
    OrderService orderService;

    @Autowired
    WaitingOrdersManager waitingOrdersManager;

    @Autowired
    TradeRepository tradeRepository;

    private final String ticker = "BTC";

    @BeforeEach
    void setUp() {
        tradeBatchProcessor.shutdown();
        waitingOrdersManager.getWaitingOrders(ticker);
        // TODO : 티커마다 큐, DB 초기화
    }

    @DisplayName("1000건의 매수 매도 주문을 요청 후 처리 성능을 조회한다.")
    @Test
    void basicLoadTestWith1000OrdersEachSide() {
        // given 1000건의 매수, 매도 주문 요청
        for (int i = 0; i < 1000; i++) {
            OrderCommand.CreateOrder sellOrderCommand = new OrderCommand.CreateOrder(ticker, 1,
                    false, false, 30.0, 40.0, LocalDateTime.now(),false);
            orderService.createOrder(sellOrderCommand);

            OrderCommand.CreateOrder buyOrderCommand = new OrderCommand.CreateOrder(ticker, 2,
                    true, false, 30.0, 40.0, LocalDateTime.now(),false);
            orderService.createOrder(buyOrderCommand);
        }
        WaitingOrders waitingOrders = waitingOrdersManager.getWaitingOrders(ticker);
        PriorityQueueStore<BuyOrder> buyOrderPriorityQueueStore = waitingOrders.getBuyOrderPriorityQueueStore(OrderType.LIMIT);
        PriorityQueueStore<SellOrder> sellOrderPriorityQueueStore = waitingOrders.getSellOrderPriorityQueueStore(OrderType.LIMIT);
        System.out.println("buyOrderPriorityQueueStore.size() : " + buyOrderPriorityQueueStore.size());
        System.out.println("sellOrderPriorityQueueStore.size() : " + sellOrderPriorityQueueStore.size());
        long testStart = System.currentTimeMillis();


        // when
        tradeBatchProcessor.run(applicationArguments);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // then
        tradeBatchProcessor.shutdown();
        long testEnd = System.currentTimeMillis();

        System.out.println("trade table size : " + tradeRepository.findAll().size());

        System.out.println("test time : " + (testEnd - testStart) + " ms");
        System.out.println("buyOrderPriorityQueueStore.size() : " + buyOrderPriorityQueueStore.size());
        System.out.println("sellOrderPriorityQueueStore.size() : " + sellOrderPriorityQueueStore.size());
    }

}
