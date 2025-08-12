package com.cleanengine.coin.orderbook.infra;

import com.cleanengine.coin.order.adapter.out.persistentce.order.command.SellOrderRepository;
import com.cleanengine.coin.order.application.dto.OrderInfo;
import com.cleanengine.coin.order.domain.SellOrder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles({"dev", "it", "h2-mem"})
@DataJpaTest
@Import({OrderBookSellOrderQueryRepository.class})
public class OrderBookSellOrderQueryRepositoryH2Test {

    @Autowired
    private OrderBookSellOrderQueryRepository orderBookSellOrderQueryRepository;

    @Autowired
    private SellOrderRepository sellOrderRepository;

    @PersistenceContext
    private EntityManager em;

    @DisplayName("저장되어 있는 SellOrder로부터 정상적으로 OrderInfo를 추출한다.")
    @Test
    @Transactional
    public void getSellOrderInfo() {
        // given
        SellOrder sellOrder = SellOrder.createLimitSellOrder(1L, "BTC", 1, 100.0, 10.0, LocalDateTime.now(), false);
        sellOrderRepository.save(sellOrder);

        em.flush();
        em.clear();

        // when
        Optional<OrderInfo.SellOrderInfo> sellOrderInfo = orderBookSellOrderQueryRepository.getSellOrderInfo(1L);

        // then
        assertTrue(sellOrderInfo.isPresent());
        assertEquals("BTC", sellOrderInfo.get().getTicker());
        assertEquals(100.0, sellOrderInfo.get().getOrderSize());
        assertEquals(10.0, sellOrderInfo.get().getPrice());
    }
}
