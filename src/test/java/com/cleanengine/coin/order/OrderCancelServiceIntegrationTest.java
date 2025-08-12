package com.cleanengine.coin.order;

import com.cleanengine.coin.common.CommonValues;
import com.cleanengine.coin.common.error.UnauthorizedAccessException;
import com.cleanengine.coin.order.adapter.out.persistentce.order.query.BuyOrderQueryRepository;
import com.cleanengine.coin.order.adapter.out.persistentce.order.query.SellOrderQueryRepository;
import com.cleanengine.coin.order.application.OrderCancelService;
import com.cleanengine.coin.order.application.OrderService;
import com.cleanengine.coin.order.application.dto.OrderCommand;
import com.cleanengine.coin.order.application.dto.OrderInfo;
import com.cleanengine.coin.order.domain.spi.ActiveOrdersManager;
import com.cleanengine.coin.order.domain.spi.WaitingOrdersManager;
import com.cleanengine.coin.trade.application.TradeExecutedEvent;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.infra.AccountQueryRepository;
import com.cleanengine.coin.user.info.infra.WalletQueryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles({"dev", "it", "h2-mem"})
@Import(OrderCancelServiceIntegrationTest.TestConfig.class)
@Transactional
public class OrderCancelServiceIntegrationTest {
    private static final String USED_COIN = "BTC";

    @Autowired
    OrderCancelService orderCancelService;

    @Autowired
    AccountQueryRepository accountQueryRepository;

    @Autowired
    WalletQueryRepository walletQueryRepository;

    @Autowired
    BuyOrderQueryRepository buyOrderQueryRepository;

    @Autowired
    ActiveOrdersManager activeOrdersManager;

    @Autowired
    WaitingOrdersManager waitingOrdersManager;

    @Autowired
    OrderService orderService;

    @Autowired
    TradeFinishedQueue tradeFinishedQueue;

    @PersistenceContext
    EntityManager em;

    @AfterEach
    public void cleanUpInMemory() {
        waitingOrdersManager.removeWaitingOrders(USED_COIN);
        activeOrdersManager.removeActiveOrders(USED_COIN);
        tradeFinishedQueue.queue.clear();

        TestTransaction.start();
        TestTransaction.flagForCommit();
        em.createNativeQuery("TRUNCATE TABLE buy_orders").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE sell_orders").executeUpdate();
        em.flush();
        em.clear();
        TestTransaction.end();
    }

    @DisplayName("userId가 일치하지 않을 때, 주문 취소가 실패한다.")
    @Test
    public void cancelOrderWithDifferentUserId_cancelOrderFails() {
        // given
        TestTransaction.flagForCommit();

        OrderCommand.CreateOrder buyOrderCommand = createOrderCommand(true, false,
                CommonValues.BUY_ORDER_BOT_ID, 100.0, 100.0);
        OrderInfo<?> orderInfo = orderService.createOrder(buyOrderCommand);
        Long orderId = orderInfo.getId();

        em.flush();
        em.clear();

        TestTransaction.end();

        // when, then
        assertThrows(UnauthorizedAccessException.class, () ->
                orderCancelService.cancelOrder(orderId, CommonValues.SELL_ORDER_BOT_ID));
    }

    @DisplayName("체결이 완료된 주문을 취소하면, 주문 취소가 실패한다.")
    @Test
    public void cancelCompletedOrder_cancelOrderFails() throws InterruptedException {
        // given
        TestTransaction.flagForCommit();

        OrderCommand.CreateOrder buyOrderCommand = createOrderCommand(true, false,
                CommonValues.BUY_ORDER_BOT_ID, 10.0, 100.0);
        OrderInfo<?> orderInfo = orderService.createOrder(buyOrderCommand);
        Long orderId = orderInfo.getId();

        OrderCommand.CreateOrder sellOrderCommand = createOrderCommand(false, false,
                CommonValues.SELL_ORDER_BOT_ID, 100.0, 100.0);
        orderService.createOrder(sellOrderCommand);

        em.flush();
        em.clear();

        TestTransaction.end();

        tradeFinishedQueue.queue.take();

        // when, then
        assertThrows(IllegalArgumentException.class, () ->
                orderCancelService.cancelOrder(orderId, CommonValues.BUY_ORDER_BOT_ID));
    }

    @DisplayName("이미 취소된 주문을 취소하면, 주문 취소가 실패한다.")
    @Test
    public void cancelCanceledOrder_cancelOrderFails() {
        // given order created
        TestTransaction.flagForCommit();

        OrderCommand.CreateOrder buyOrderCommand = createOrderCommand(true, false,
                CommonValues.BUY_ORDER_BOT_ID, 10.0, 100.0);
        OrderInfo<?> orderInfo = orderService.createOrder(buyOrderCommand);
        Long orderId = orderInfo.getId();

        em.flush();
        em.clear();

        TestTransaction.end();

        // given order canceled
        TestTransaction.start();
        TestTransaction.flagForCommit();

        orderCancelService.cancelOrder(orderId, CommonValues.BUY_ORDER_BOT_ID);

        em.flush();
        em.clear();

        TestTransaction.end();

        // when, then
        assertThrows(IllegalArgumentException.class, () ->
                orderCancelService.cancelOrder(orderId, CommonValues.BUY_ORDER_BOT_ID));
    }

    @DisplayName("매수 주문을 취소하면, 사용자의 계좌에 잔여금액 만큼의 돈이 복원된다.")
    @Test
    public void cancelBuyOrder_increaseCashSuccessfully() {
        // given
        TestTransaction.flagForCommit();

        Double price = 100.0;
        Double buyOrderSize = 100.0;
        Double lockedDeposit = buyOrderSize * price;

        OrderCommand.CreateOrder buyOrderCommand = createOrderCommand(true, false,
                CommonValues.BUY_ORDER_BOT_ID, buyOrderSize, price);
        OrderInfo<?> orderInfo = orderService.createOrder(buyOrderCommand);
        Long orderId = orderInfo.getId();

        Account initialAccount = accountQueryRepository.findByUserId(CommonValues.BUY_ORDER_BOT_ID).orElseThrow();
        Double decreasedBalance = initialAccount.getCash();

        em.flush();
        em.clear();

        TestTransaction.end();

        // when
        orderCancelService.cancelOrder(orderId, CommonValues.BUY_ORDER_BOT_ID);

        // then
        Account accountAfterCancel = accountQueryRepository.findByUserId(CommonValues.BUY_ORDER_BOT_ID).orElseThrow();
        assertEquals(decreasedBalance + lockedDeposit, accountAfterCancel.getCash());
    }

    @DisplayName("매도 주문을 취소하면, 사용자의 지갑에 잔여량 만큼의 가상화폐가 복원된다.")
    @Test
    public void cancelSellOrder_increaseAssetSuccessfully() {
        // given
        TestTransaction.flagForCommit();

        Double price = 100.0;
        Double sellOrderSize = 100.0;

        OrderCommand.CreateOrder sellOrderCommand = createOrderCommand(false, false,
                CommonValues.SELL_ORDER_BOT_ID, sellOrderSize, price);
        OrderInfo<?> orderInfo = orderService.createOrder(sellOrderCommand);
        Long orderId = orderInfo.getId();

        Wallet initialWallet = walletQueryRepository.findByUserIdAndTicker(CommonValues.SELL_ORDER_BOT_ID, USED_COIN).orElseThrow();
        Double decreasedAmount = initialWallet.getSize();

        em.flush();
        em.clear();

        TestTransaction.end();

        // when
        orderCancelService.cancelOrder(orderId, CommonValues.SELL_ORDER_BOT_ID);

        // then
        Wallet walletAfterCancel = walletQueryRepository.findByUserIdAndTicker(CommonValues.SELL_ORDER_BOT_ID, USED_COIN).orElseThrow();
        assertEquals(decreasedAmount + sellOrderSize, walletAfterCancel.getSize());
    }

    @DisplayName("주문을 취소하면, db상 주문의 상태가 성공적으로 변경된다.")
    @Test
    public void cancelOrder_orderCanceledAppliedInDBSuccessfully() {
        // given
        TestTransaction.flagForCommit();

        Double price = 100.0;
        Double buyOrderSize = 100.0;

        OrderCommand.CreateOrder buyOrderCommand = createOrderCommand(true, false,
                CommonValues.BUY_ORDER_BOT_ID, buyOrderSize, price);
        OrderInfo<?> orderInfo = orderService.createOrder(buyOrderCommand);
        Long orderId = orderInfo.getId();

        em.flush();
        em.clear();

        TestTransaction.end();

        // when
        orderCancelService.cancelOrder(orderId, CommonValues.BUY_ORDER_BOT_ID);

        // then
        int orderCount = buyOrderQueryRepository.findIncompletedBuyOrders().size();
        assertEquals(0, orderCount);

    }

    private OrderCommand.CreateOrder createOrderCommand(boolean isBuyOrder, boolean isMarketOrder, Integer userId,
                                                        Double orderSize, Double price) {
        return new OrderCommand.CreateOrder(
                USED_COIN,
                userId,
                isBuyOrder,
                isMarketOrder,
                orderSize,
                price,
                false
        );
    }

    static class TradeExecutedHandlerForTest {

        @Autowired
        private TradeFinishedQueue tradeFinishedQueue;

        @TransactionalEventListener
        public void handle(TradeExecutedEvent tradeExecutedEvent) {
            tradeFinishedQueue.queue.add(tradeExecutedEvent);
        }
    }

    static class TradeFinishedQueue {
        public BlockingQueue<TradeExecutedEvent> queue = new LinkedBlockingQueue<>();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public OrderCancelServiceIntegrationTest.TradeFinishedQueue tradeFinishedQueue() {
            return new OrderCancelServiceIntegrationTest.TradeFinishedQueue();
        }

        @Bean
        public OrderCancelServiceIntegrationTest.TradeExecutedHandlerForTest tradeExecutedHandlerForTest() {
            return new OrderCancelServiceIntegrationTest.TradeExecutedHandlerForTest();
        }
    }
}



