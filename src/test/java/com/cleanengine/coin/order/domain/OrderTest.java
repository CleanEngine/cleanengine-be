package com.cleanengine.coin.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.cleanengine.coin.order.domain.tool.BuyOrderGenerator.LimitBuyOrderGenerator.createLimitBuyOrderWithRandomPrice;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("주문(Order) 엔티티 테스트")
class OrderTest {
    @Nested
    @DisplayName("equals 테스트")
    class EqualsTest{
        @DisplayName("같은 주문 객체에 대해 equals 연산을 수행하면, true를 반환한다.")
        @Test
        void equalsSameObject_returnTrue() {
            BuyOrder buyOrder = new BuyOrder(1L, "BTC", 1, OrderStatus.WAIT, 1.0, 1.0, 1.0, null, false, false, 1.0, 1.0);

            assertTrue(buyOrder.equals(buyOrder));

        }
        @DisplayName("null인 주문 객체에 대해 equals 연산을 수행하면, false를 반환한다.")
        @Test
        void equalsNull_returnFalse() {
            BuyOrder buyOrder = new BuyOrder(1L, "BTC", 1, OrderStatus.WAIT, 1.0, 1.0, 1.0, null, false, false, 1.0, 1.0);
            BuyOrder nullBuyOrder = null;

            assertFalse(buyOrder.equals(nullBuyOrder));

        }
        @DisplayName("id가 같은 매도 주문과 매수주문에 대해 equals 연산을 수행하면, false를 반환한다.")
        @Test
        void equalsSameIdBuyOrderAndSellOrder_returnFalse() {
            BuyOrder buyOrder = new BuyOrder(1L, "BTC", 1, OrderStatus.WAIT, 1.0, 1.0, 1.0, null, false, false, 1.0, 1.0);
            SellOrder sellOrder = new SellOrder(1L, "BTC", 1, OrderStatus.WAIT, 1.0, 1.0, 1.0, null, false, false);

            assertFalse(buyOrder.equals(sellOrder));
        }
        @DisplayName("id가 같은 매수주문 객체에 대해 equals 연산을 수행하면, true를 반환한다.")
        @Test
        void equalsSameIdBuyOrder_returnTrue() {
            BuyOrder buyOrder = new BuyOrder(1L, "BTC", 1, OrderStatus.WAIT, 1.0, 1.0, 1.0, null, false, false, 1.0, 1.0);
            BuyOrder buyOrder2 = new BuyOrder(1L, "BTC", 1, OrderStatus.WAIT, 2.0, 2.0, 2.0, null, false, false, 2.0, 2.0);

            assertTrue(buyOrder.equals(buyOrder2));
        }
        @DisplayName("id가 다른 매수주문 객체에 대해 equals 연산을 수행하면, false를 반환한다.")
        @Test
        void equalsDifferentIdBuyOrder_returnFalse() {
            BuyOrder buyOrder = new BuyOrder(1L, "BTC", 1, OrderStatus.WAIT, 1.0, 1.0, 1.0, null, false, false, 1.0, 1.0);
            BuyOrder buyOrder2 = new BuyOrder(2L, "BTC", 1, OrderStatus.WAIT, 1.0, 1.0, 1.0, null, false, false, 1.0, 1.0);

            assertFalse(buyOrder.equals(buyOrder2));
        }
        @DisplayName("id가 같은 매도주문 객체에 대해 equals 연산을 수행하면, true를 반환한다.")
        @Test
        void equalsSameIdSellOrder_returnTrue() {
            SellOrder sellOrder = new SellOrder(1L, "BTC", 1, OrderStatus.WAIT, 1.0, 1.0, 1.0, null, false, false);
            SellOrder sellOrder2 = new SellOrder(1L, "BTC", 1, OrderStatus.WAIT, 2.0, 2.0, 2.0, null, false, false);

            assertTrue(sellOrder.equals(sellOrder2));
        }
        @DisplayName("id가 다른 매도주문 객체에 대해 equals 연산을 수행하면, false를 반환한다.")
        @Test
        void equalsDifferentIdSellOrder_returnFalse() {
            SellOrder sellOrder = new SellOrder(1L, "BTC", 1, OrderStatus.WAIT, 1.0, 1.0, 1.0, null, false, false);
            SellOrder sellOrder2 = new SellOrder(2L, "BTC", 1, OrderStatus.WAIT, 1.0, 1.0, 1.0, null, false, false);

            assertFalse(sellOrder.equals(sellOrder2));
        }
    }

    @Nested
    @DisplayName("hashCode 테스트")
    class HashCodeTest{
        @DisplayName("매도 주문이 id가 같으면 hashcode는 같은 값이어야 한다.")
        @Test
        void sameIdSellOrders_returnSameHashCode() {
            SellOrder sellOrder1 = new SellOrder(1L, "BTC", 1, OrderStatus.WAIT, 1.0, 1.0, 1.0, null, false, false);
            SellOrder sellOrder2 = new SellOrder(1L, "BTC", 1, OrderStatus.WAIT, 2.0, 2.0, 2.0, null, false, false);

            assertEquals(sellOrder1.hashCode(), sellOrder2.hashCode());
        }

        @DisplayName("매수 주문이 id가 같으면 hashcode는 같은 값이어야 한다.")
        @Test
        void sameIdBuyOrders_returnSameHashCode() {
            BuyOrder buyOrder1 = new BuyOrder(1L, "BTC", 1, OrderStatus.WAIT, 1.0, 1.0, 1.0, null, false, false, 1.0, 1.0);
            BuyOrder buyOrder2 = new BuyOrder(1L, "BTC", 1, OrderStatus.WAIT, 2.0, 2.0, 2.0, null, false, false, 2.0, 2.0);

            assertEquals(buyOrder1.hashCode(), buyOrder2.hashCode());
        }
    }

    @Nested
    @DisplayName("setState 테스트")
    class SetStateTest{
        @DisplayName("null인 orderState로 setState를 하면, Exception을 반환한다.")
        @Test
        void setNullOrderState_throwIllegalArgumentException() {
            BuyOrder buyOrder = createLimitBuyOrderWithRandomPrice();
            OrderStatus nullState = null;

            assertThrows(IllegalArgumentException.class, () -> buyOrder.setState(nullState));
        }
    }

    @Nested
    @DisplayName("decreaseRemainingSize 테스트")
    class DecreaseRemainingSizeTest{
        @DisplayName("null인 amount로 decreaseRemainingSize 호출시, Exception을 반환한다.")
        @Test
        void decreaseRemainingSizeWithNullAmount_throwsException() {
            BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(1L, "BTC", 1, 100.0, 10.0, LocalDateTime.now(), false);

            assertThrows(IllegalArgumentException.class, () -> buyOrder.decreaseRemainingSize(null));
        }

        @DisplayName("remainingSize보다 큰 amount로 decreaseRemainingSize 호출시, Exception을 반환한다.")
        @Test
        void decreaseRemainingSizeWithBiggerAmount_throwsException() {
            BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(1L, "BTC", 1, 100.0, 10.0, LocalDateTime.now(), false);

            assertThrows(IllegalArgumentException.class, () -> buyOrder.decreaseRemainingSize(200.0));
        }

        @DisplayName("remainingSize보다 작은 amount로 decreaseRemainingSize 호출시, 정상 적용된다.")
        @Test
        void decreaseRemainingSizeWithSmallerAmount_resultAsExpected() {
            BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(1L, "BTC", 1, 100.0, 10.0, LocalDateTime.now(), false);

            buyOrder.decreaseRemainingSize(90.0);

            assertEquals(10.0, buyOrder.getRemainingSize());
        }
    }
}