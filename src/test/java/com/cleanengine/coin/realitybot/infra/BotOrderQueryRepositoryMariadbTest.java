package com.cleanengine.coin.realitybot.infra;

import com.cleanengine.coin.base.MariaDBAdapterTest;
import com.cleanengine.coin.configuration.QueryDslConfig;
import com.cleanengine.coin.order.adapter.out.persistentce.order.command.BuyOrderRepository;
import com.cleanengine.coin.order.adapter.out.persistentce.order.command.SellOrderRepository;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.OrderStatus;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.realitybot.dto.BotOrderCount;
import com.cleanengine.coin.realitybot.dto.BotOrderInfo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.cleanengine.coin.common.CommonValues.BUY_ORDER_BOT_ID;
import static com.cleanengine.coin.common.CommonValues.SELL_ORDER_BOT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Import({QueryDslConfig.class, BotOrderQueryRepository.class})
public class BotOrderQueryRepositoryMariadbTest extends MariaDBAdapterTest {
    private static final String USED_TICKER = "BTC";

    @Autowired
    private BotOrderQueryRepository botOrderQueryRepository;

    @Autowired
    private BuyOrderRepository buyOrderRepository;

    @Autowired
    private SellOrderRepository sellOrderRepository;

    @PersistenceContext
    private EntityManager em;

    @DisplayName("countWaitingBotOrdersByTicker 테스트")
    @Nested
    class CountTest {

        @DisplayName("null인 ticker로 count 호출시 IllegalArgumentException를 던진다.")
        @Test
        public void countWithNullTicker_throwIllegalArgumentException() {
            String nullTicker = null;

            assertThrows(IllegalArgumentException.class, () -> {
                botOrderQueryRepository.countWaitingBotOrdersByTicker(nullTicker);
            });
        }

        @DisplayName("조건에 맞는 데이터가 있을 경우, 정상적으로 count 결과를 반환한다.")
        @Test
        public void countWithValidOrders_returnCorrectCount() {
//            TestTransaction.flagForCommit();

            BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(1L, USED_TICKER, BUY_ORDER_BOT_ID, 5.0, 500.0, LocalDateTime.now(), true);
            buyOrderRepository.save(buyOrder);

            SellOrder sellOrder = SellOrder.createLimitSellOrder(2L, USED_TICKER, SELL_ORDER_BOT_ID, 5.0, 500.0, LocalDateTime.now(), true);
            sellOrderRepository.save(sellOrder);

            final AtomicReference<Connection> jpaConnectionRef = new AtomicReference<>();

            Session session = em.unwrap(Session.class);
            session.doWork(connection -> {
                System.out.println(">>> JPA (via doWork) Connection is: " + connection);
                jpaConnectionRef.set(connection);
            });

            Connection connection = jpaConnectionRef.get();
            System.out.println("### main Connection: " + connection);
            em.flush();
            em.clear();

//            TestTransaction.end();

//            TestTransaction.start();

            System.out.println(buyOrderRepository.count());
            System.out.println(sellOrderRepository.count());

            BotOrderCount botOrderCount = botOrderQueryRepository.countWaitingBotOrdersByTicker(USED_TICKER);

            assertEquals(1L, botOrderCount.buyOrderCount());
            assertEquals(1L, botOrderCount.sellOrderCount());
        }

        @DisplayName("WAIT 상태가 아닌 주문 데이터들은 count 대상에서 제외된다.")
        @Test
        public void countWithNonWaitOrders_excludedFromCount() {
            BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(1L, USED_TICKER, BUY_ORDER_BOT_ID, 5.0, 500.0, LocalDateTime.now(), true);
            buyOrder.setState(OrderStatus.DONE);
            buyOrderRepository.save(buyOrder);

            SellOrder sellOrder = SellOrder.createLimitSellOrder(2L, USED_TICKER, SELL_ORDER_BOT_ID, 5.0, 500.0, LocalDateTime.now(), true);
            sellOrder.setState(OrderStatus.DONE);
            sellOrderRepository.save(sellOrder);

            em.flush();
            em.clear();

            BotOrderCount botOrderCount = botOrderQueryRepository.countWaitingBotOrdersByTicker(USED_TICKER);

            assertEquals(0L, botOrderCount.buyOrderCount());
            assertEquals(0L, botOrderCount.sellOrderCount());
        }

        @DisplayName("지정한 ticker가 아닌 주문은 count 대상에서 제외된다")
        @Test
        public void countWithOtherTickerOrders_excludedFromCount() {
            BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(1L, "ETH", BUY_ORDER_BOT_ID, 5.0, 500.0, LocalDateTime.now(), true);
            buyOrderRepository.save(buyOrder);

            SellOrder sellOrder = SellOrder.createLimitSellOrder(2L, "ETH", SELL_ORDER_BOT_ID, 5.0, 500.0, LocalDateTime.now(), true);
            sellOrderRepository.save(sellOrder);

            em.flush();
            em.clear();

            BotOrderCount botOrderCount = botOrderQueryRepository.countWaitingBotOrdersByTicker(USED_TICKER);

            assertEquals(0L, botOrderCount.buyOrderCount());
            assertEquals(0L, botOrderCount.sellOrderCount());
        }

        @DisplayName("지정된 BOT ID가 아닌 사용자가 생성한 주문은 count 대상에서 제외된다")
        @Test
        public void countWithOtherBotIdOrders_excludedFromCount() {
            BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(1L, USED_TICKER, 3, 5.0, 500.0, LocalDateTime.now(), true);
            buyOrderRepository.save(buyOrder);

            SellOrder sellOrder = SellOrder.createLimitSellOrder(2L, USED_TICKER, 3, 5.0, 500.0, LocalDateTime.now(), true);
            sellOrderRepository.save(sellOrder);

            em.flush();
            em.clear();

            BotOrderCount botOrderCount = botOrderQueryRepository.countWaitingBotOrdersByTicker(USED_TICKER);

            assertEquals(0L, botOrderCount.buyOrderCount());
            assertEquals(0L, botOrderCount.sellOrderCount());
        }
    }

    @DisplayName("findWaitingBotOrdersByTickerAndRate 테스트")
    @Nested
    class FindBotOrdersTest {

        @DisplayName("ticker가 null인 경우 IllegalArgumentException를 던진다.")
        @Test
        public void findWithNullTicker_throwIllegalArgumentException() {
            String nullTicker = null;
            BotOrderCount botOrderCount = new BotOrderCount(0, 0);

            assertThrows(IllegalArgumentException.class, () -> botOrderQueryRepository.findWaitingBotOrdersByTickerAndCount(nullTicker, botOrderCount));
        }

        @DisplayName("botOrderCount가 null인 경우 IllegalArgumentException를 던진다.")
        @Test
        public void findWithNullBotOrderCount_throwIllegalArgumentException() {
            BotOrderCount nullBotOrderCount = null;

            assertThrows(IllegalArgumentException.class, () -> botOrderQueryRepository.findWaitingBotOrdersByTickerAndCount(USED_TICKER, nullBotOrderCount));
        }

        @DisplayName("조건에 맞는 데이터가 있을 경우, Order의 정보를 정상적으로 불러온다.")
        @Test
        public void findWithValidOrders_returnCorrectBotInfos() {
            BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(1L, USED_TICKER, BUY_ORDER_BOT_ID, 5.0, 500.0, LocalDateTime.now(), true);
            buyOrderRepository.save(buyOrder);

            SellOrder sellOrder = SellOrder.createLimitSellOrder(2L, USED_TICKER, SELL_ORDER_BOT_ID, 5.0, 500.0, LocalDateTime.now(), true);
            sellOrderRepository.save(sellOrder);

            em.flush();
            em.clear();

            BotOrderCount botOrderCount = new BotOrderCount(1, 1);
            List<BotOrderInfo> botOrderInfos = botOrderQueryRepository.findWaitingBotOrdersByTickerAndCount(USED_TICKER, botOrderCount);

            assertEquals(2, botOrderInfos.size());

            assertEquals(buyOrder.getUserId(), botOrderInfos.get(0).userId());
            assertEquals(buyOrder.getTicker(), botOrderInfos.get(0).ticker());
            assertEquals(buyOrder.getId(), botOrderInfos.get(0).orderId());

            assertEquals(sellOrder.getUserId(), botOrderInfos.get(1).userId());
            assertEquals(sellOrder.getTicker(), botOrderInfos.get(1).ticker());
            assertEquals(sellOrder.getId(), botOrderInfos.get(1).orderId());
        }

        @DisplayName("WAIT 상태가 아닌 주문들은 find 대상에서 제외된다.")
        @Test
        public void findWithNonWaitOrders_excludedFromFind() {
            BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(1L, USED_TICKER, BUY_ORDER_BOT_ID, 5.0, 500.0, LocalDateTime.now(), true);
            buyOrder.setState(OrderStatus.DONE);
            buyOrderRepository.save(buyOrder);

            SellOrder sellOrder = SellOrder.createLimitSellOrder(2L, USED_TICKER, SELL_ORDER_BOT_ID, 5.0, 500.0, LocalDateTime.now(), true);
            sellOrder.setState(OrderStatus.DONE);
            sellOrderRepository.save(sellOrder);

            em.flush();
            em.clear();

            BotOrderCount botOrderCount = new BotOrderCount(1, 1);
            List<BotOrderInfo> botOrderInfos = botOrderQueryRepository.findWaitingBotOrdersByTickerAndCount(USED_TICKER, botOrderCount);

            assertEquals(0, botOrderInfos.size());
        }

        @DisplayName("지정한 ticker가 아닌 주문은 find 대상에서 제외된다")
        @Test
        public void findWithOtherTickerOrders_excludedFromFind() {
            BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(1L, "ETH", BUY_ORDER_BOT_ID, 5.0, 500.0, LocalDateTime.now(), true);
            buyOrderRepository.save(buyOrder);

            SellOrder sellOrder = SellOrder.createLimitSellOrder(2L, "ETH", SELL_ORDER_BOT_ID, 5.0, 500.0, LocalDateTime.now(), true);
            sellOrderRepository.save(sellOrder);

            em.flush();
            em.clear();

            BotOrderCount botOrderCount = new BotOrderCount(1, 1);
            List<BotOrderInfo> botOrderInfos = botOrderQueryRepository.findWaitingBotOrdersByTickerAndCount(USED_TICKER, botOrderCount);

            assertEquals(0, botOrderInfos.size());
        }

        @DisplayName("지정된 BOT ID가 아닌 사용자가 생성한 주문은 find 대상에서 제외된다")
        @Test
        public void findWithOtherBotIdOrders_excludedFromFind() {
            BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(1L, USED_TICKER, 3, 5.0, 500.0, LocalDateTime.now(), true);
            buyOrderRepository.save(buyOrder);

            SellOrder sellOrder = SellOrder.createLimitSellOrder(2L, USED_TICKER, 3, 5.0, 500.0, LocalDateTime.now(), true);
            sellOrderRepository.save(sellOrder);

            em.flush();
            em.clear();

            BotOrderCount botOrderCount = new BotOrderCount(1, 1);
            List<BotOrderInfo> botOrderInfos = botOrderQueryRepository.findWaitingBotOrdersByTickerAndCount(USED_TICKER, botOrderCount);

            assertEquals(0, botOrderInfos.size());
        }

        @DisplayName("조회 대상 매수 주문이 여러개인 경우, BotOrderInfo는 가격이 낮은 순서대로 정렬된다.")
        @Test
        public void findWithMultipleBuyOrders_returnCheapestFirst() {
            BuyOrder expensiveBuyOrder = BuyOrder.createLimitBuyOrder(3L, USED_TICKER, BUY_ORDER_BOT_ID, 5.0, 600.0, LocalDateTime.now(), true);
            BuyOrder normalBuyOrder = BuyOrder.createLimitBuyOrder(2L, USED_TICKER, BUY_ORDER_BOT_ID, 5.0, 500.0, LocalDateTime.now(), true);
            BuyOrder cheapBuyOrder = BuyOrder.createLimitBuyOrder(1L, USED_TICKER, BUY_ORDER_BOT_ID, 5.0, 400.0, LocalDateTime.now(), true);

            buyOrderRepository.saveAll(List.of(expensiveBuyOrder, normalBuyOrder, cheapBuyOrder));

            em.flush();
            em.clear();

            BotOrderCount botOrderCount = new BotOrderCount(2, 0);
            List<BotOrderInfo> botOrderInfos = botOrderQueryRepository.findWaitingBotOrdersByTickerAndCount(USED_TICKER, botOrderCount);

            assertEquals(2, botOrderInfos.size());
            assertEquals(cheapBuyOrder.getId(), botOrderInfos.get(0).orderId());
            assertEquals(normalBuyOrder.getId(), botOrderInfos.get(1).orderId());
        }

        @DisplayName("조회 대상 매도 주문이 여러개인 경우, BotOrderInfo는 가격이 비싼 순서대로 정렬된다.")
        @Test
        public void findWithMultipleSellOrders_returnExpensiveFirst() {
            SellOrder expensiveSellOrder = SellOrder.createLimitSellOrder(3L, USED_TICKER, SELL_ORDER_BOT_ID, 5.0, 600.0, LocalDateTime.now(), true);
            SellOrder normalSellOrder = SellOrder.createLimitSellOrder(2L, USED_TICKER, SELL_ORDER_BOT_ID, 5.0, 500.0, LocalDateTime.now(), true);
            SellOrder cheapSellOrder = SellOrder.createLimitSellOrder(1L, USED_TICKER, SELL_ORDER_BOT_ID, 5.0, 400.0, LocalDateTime.now(), true);

            sellOrderRepository.saveAll(List.of(expensiveSellOrder, normalSellOrder, cheapSellOrder));

            em.flush();
            em.clear();

            BotOrderCount botOrderCount = new BotOrderCount(0, 2);
            List<BotOrderInfo> botOrderInfos = botOrderQueryRepository.findWaitingBotOrdersByTickerAndCount(USED_TICKER, botOrderCount);

            assertEquals(2, botOrderInfos.size());
            assertEquals(expensiveSellOrder.getId(), botOrderInfos.get(0).orderId());
            assertEquals(normalSellOrder.getId(), botOrderInfos.get(1).orderId());
        }
    }
}
