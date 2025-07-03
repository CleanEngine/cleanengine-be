package com.cleanengine.coin.order.application.strategy;

import com.cleanengine.coin.base.MariaDBIntegrationTest;
import com.cleanengine.coin.common.CommonValues;
import com.cleanengine.coin.order.adapter.out.persistentce.order.command.BuyOrderRepository;
import com.cleanengine.coin.order.application.dto.OrderCommand;
import com.cleanengine.coin.order.application.dto.OrderInfo;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.tool.helper.TestClockHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.cleanengine.coin.common.CommonValues.BASE_EPOCH_TIME_MILLIS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(BuyOrderStrategyMariaDBIntegrationTest.TestClockConfig.class)
public class BuyOrderStrategyMariaDBIntegrationTest extends MariaDBIntegrationTest {
    @Autowired
    BuyOrderStrategy buyOrderStrategy;

    @Autowired
    BuyOrderRepository buyOrderRepository;

    @Autowired
    TestClockHolder testClockHolder;

    @Sql("classpath:com/cleanengine/coin/order/application/initializeBotUser.sql")
    @DisplayName("정상적인 입력값을 가진 매수주문이 MariaDB repository를 통해 기대한대로 저장됨")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Test
    void saveValidBuyOrder_savedAsExpected() {
        OrderCommand.CreateOrder createOrderCommand =
                new OrderCommand.CreateOrder("BTC", CommonValues.BUY_ORDER_BOT_ID,true, false, 100.0, 100.0,false);

        OrderInfo buyOrderInfo = buyOrderStrategy.processCreatingOrder(createOrderCommand);

        em.flush();
        em.clear();

        Optional<BuyOrder> orderOpt = buyOrderRepository.findById(buyOrderInfo.getId());

        assertTrue(orderOpt.isPresent());

        BuyOrder order = orderOpt.get();

        LocalDateTime baseEpochTime = LocalDateTime.of(2025,1,1,9,0,0);
        assertEquals(baseEpochTime, order.getCreatedAt());
    }

    @TestConfiguration
    static class TestClockConfig {

        @Bean
        @Primary
        public TestClockHolder testClockHolder() {
            return new TestClockHolder(BASE_EPOCH_TIME_MILLIS);
        }
    }
}
