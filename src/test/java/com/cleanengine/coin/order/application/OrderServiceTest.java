package com.cleanengine.coin.order.application;

import com.cleanengine.coin.base.MariaDBIntegrationTest;
import com.cleanengine.coin.common.CommonValues;
import com.cleanengine.coin.order.adapter.out.persistentce.order.command.BuyOrderRepository;
import com.cleanengine.coin.order.adapter.out.persistentce.order.command.SellOrderRepository;
import com.cleanengine.coin.order.application.dto.OrderCommand;
import com.cleanengine.coin.trade.repository.TradeRepository;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.info.infra.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderServiceTest extends MariaDBIntegrationTest {
    @Autowired
    OrderService orderService;

    @Autowired
    TradeRepository tradeRepository;

    @Autowired
    BuyOrderRepository buyOrderRepository;
    @Autowired
    SellOrderRepository sellOrderRepository;

    @Autowired
    AccountRepository accountRepository;
    
    @Sql("initializeBotUser.sql")
    @DisplayName("동시에 5개의 매도요청과 5개의 매수요청이 들어왔을 때 주문에 대한 체결이 정상적으로 처리된다.")
    @Test
    public void create10OrdersSimultaneously_orderShouldBeProcessedSuccessfully() throws InterruptedException {

        int numberOfThreads = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);

        OrderCommand.CreateOrder buyOrderCommand = new OrderCommand.CreateOrder("BTC", CommonValues.BUY_ORDER_BOT_ID,true, false, 100.0, 100.0,false);
        OrderCommand.CreateOrder sellOrderCommand = new OrderCommand.CreateOrder("BTC", CommonValues.SELL_ORDER_BOT_ID,false, false, 100.0, 100.0,false);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try{
                    startLatch.await();
                    orderService.createOrder(buyOrderCommand);
                    orderService.createOrder(buyOrderCommand);
                    orderService.createOrder(buyOrderCommand);
                    orderService.createOrder(buyOrderCommand);
                    orderService.createOrder(buyOrderCommand);
                    orderService.createOrder(buyOrderCommand);
                    orderService.createOrder(buyOrderCommand);
                    orderService.createOrder(buyOrderCommand);
                    orderService.createOrder(buyOrderCommand);
                    orderService.createOrder(buyOrderCommand);
//                    orderService.createOrder(sellOrderCommand);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        endLatch.await();
        // 비동기적으로 처리되는 체결이 완료될때까지 대기
        Thread.sleep(2000);

        long resultCount = tradeRepository.count();
        long buyOrderCount = buyOrderRepository.count();
        long sellOrderCount = sellOrderRepository.count();

        System.out.println(buyOrderCount);
        System.out.println(sellOrderCount);
//        extracted();
        assertEquals(5, resultCount);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void extracted() {
        Account account = accountRepository.findByUserId(CommonValues.BUY_ORDER_BOT_ID).get();
        System.out.println(account.getCash());
    }
}
