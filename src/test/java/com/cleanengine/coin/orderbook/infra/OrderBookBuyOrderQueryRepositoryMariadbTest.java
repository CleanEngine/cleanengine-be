package com.cleanengine.coin.orderbook.infra;

import com.cleanengine.coin.base.MariaDBAdapterTest;
import com.cleanengine.coin.order.adapter.out.persistentce.order.command.BuyOrderRepository;
import com.cleanengine.coin.order.application.dto.OrderInfo;
import com.cleanengine.coin.order.domain.BuyOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({
        OrderBookBuyOrderQueryRepository.class
})
public class OrderBookBuyOrderQueryRepositoryMariadbTest extends MariaDBAdapterTest {

    @Autowired
    private OrderBookBuyOrderQueryRepository orderBookBuyOrderQueryRepository;

    @Autowired
    private BuyOrderRepository buyOrderRepository;

    @DisplayName("저장되어 있는 BuyOrder로부터 정상적으로 OrderInfo를 추출한다.")
    @Test
    @Transactional
    public void getBuyOrderInfo() {
        // given
        BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(1L, "BTC", 1, 100.0, 10.0, LocalDateTime.now(), false);
        buyOrderRepository.save(buyOrder);

        em.flush();
        em.clear();

        // when
        Optional<OrderInfo.BuyOrderInfo> buyOrderInfo = orderBookBuyOrderQueryRepository.getBuyOrderInfo(1L);

        // then
        assertTrue(buyOrderInfo.isPresent());
        assertEquals("BTC", buyOrderInfo.get().getTicker());
        assertEquals(100.0, buyOrderInfo.get().getOrderSize());
        assertEquals(10.0, buyOrderInfo.get().getPrice());
    }
}
