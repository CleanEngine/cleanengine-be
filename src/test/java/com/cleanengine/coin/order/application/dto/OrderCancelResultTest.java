package com.cleanengine.coin.order.application.dto;

import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.OrderStatus;
import com.cleanengine.coin.order.domain.SellOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class OrderCancelResultTest {
    @DisplayName("미체결 지정가 매수 주문 기본 매핑 검증")
    @Test
    public void validateLimitBuyOrder() {
        BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(1L, "BTC", 1, 100.0, 10.0, LocalDateTime.now(), false);

        OrderCancelResult orderCancelResult = new OrderCancelResult(buyOrder);

        assertEquals("BTC", orderCancelResult.ticker());
        assertEquals(1L, orderCancelResult.orderId());
        assertEquals(OrderCancelResult.ExecutionStatus.NONE, orderCancelResult.executionStatus());
        assertEquals("bid", orderCancelResult.side());
        assertEquals("limit", orderCancelResult.orderType());
        assertEquals(10.0, orderCancelResult.price());
        assertEquals(100.0, orderCancelResult.remainingSize());
    }

    @DisplayName("미체결 시장가 매도 주문 기본 매핑 검증")
    @Test
    public void validateMarketSellOrder() {
        SellOrder sellOrder = SellOrder.createMarketSellOrder(1L, "BTC", 1, 100.0, LocalDateTime.now(), false);

        OrderCancelResult orderCancelResult = new OrderCancelResult(sellOrder);

        assertEquals("BTC", orderCancelResult.ticker());
        assertEquals(1L, orderCancelResult.orderId());
        assertEquals(OrderCancelResult.ExecutionStatus.NONE, orderCancelResult.executionStatus());
        assertEquals("market", orderCancelResult.orderType());
        assertNull(orderCancelResult.price());
        assertEquals(100.0, orderCancelResult.remainingSize());
    }

    @DisplayName("잔량이 일부 감소한 매도 주문의 Status는 부분 체결 상태여야 한다.")
    @Test
    public void sellOrderWithDecreasedSize_hasPartialExecutedStatus() {
        SellOrder sellOrder = SellOrder.createLimitSellOrder(1L, "BTC", 1, 100.0, 10.0, LocalDateTime.now(), false);
        sellOrder.decreaseRemainingSize(10.0);

        OrderCancelResult orderCancelResult = new OrderCancelResult(sellOrder);

        assertEquals(OrderCancelResult.ExecutionStatus.PARTIAL_EXECUTED, orderCancelResult.executionStatus());
    }

    @DisplayName("잔액이 일부 감소한 매수 주문의 Status는 부분 체결 상태여야 한다.")
    @Test
    public void buyOrderWithDecreasedSize_hasPartialExecutedStatus() {
        BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(1L, "BTC", 1, 100.0, 10.0, LocalDateTime.now(), false);
        buyOrder.decreaseRemainingDeposit(10.0);

        OrderCancelResult orderCancelResult = new OrderCancelResult(buyOrder);

        assertEquals(OrderCancelResult.ExecutionStatus.PARTIAL_EXECUTED, orderCancelResult.executionStatus());
    }

    @DisplayName("상태가 Done인 주문 객체의 Status는 전체 체결 상태여야 한다.")
    @Test
    public void doneOrder_hasAllExecutedStatus() {
        SellOrder sellOrder = SellOrder.createLimitSellOrder(1L, "BTC", 1, 100.0, 10.0, LocalDateTime.now(), false);
        sellOrder.setState(OrderStatus.DONE);

        OrderCancelResult orderCancelResult = new OrderCancelResult(sellOrder);
        assertEquals(OrderCancelResult.ExecutionStatus.ALL_EXECUTED, orderCancelResult.executionStatus());
    }
}
