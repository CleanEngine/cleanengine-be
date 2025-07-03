package com.cleanengine.coin.order.domain;

import com.cleanengine.coin.common.error.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SellOrderTest {
    protected static final LocalDateTime baseTime = LocalDateTime.now();

    @Nested
    @DisplayName("시장가 매도주문 생성 테스트")
    class CreateMarketSellOrderTest{
        @DisplayName("orderSize가 null인 시장가 매도주문을 생성시 Exception을 반환함")
        @Test
        public void createMarketSellOrderWithNullOrderSize_throwException(){
            Double nullOrderSize = null;

            assertThrows(DomainValidationException.class, ()->{
                SellOrder.createMarketSellOrder(1L, "BTC", 1, nullOrderSize, baseTime, false);
            });
        }

        @DisplayName("orderSize가 null이 아닌 시장가 매도 주문을 생성시 remainingSize 관련 필드가 정상적으로 초기화 됨")
        @Test
        public void createMarketSellOrder_initializeRemainingSizeCorrectly() {
            Double orderSize = 10.0;

            SellOrder sellOrder = SellOrder.createMarketSellOrder(1L, "BTC", 1, orderSize, baseTime, false);

            assertEquals(orderSize, sellOrder.getRemainingSize());
        }

        @DisplayName("시장가 매도 주문 생성시 OrderStatus가 WAIT로 초기화 됨")
        @Test
        public void createMarketSellOrder_initializeOrderStatusWithWait() {
            SellOrder sellOrder = SellOrder.createMarketSellOrder(1L, "BTC", 1, 10.0, baseTime, false);

            assertEquals(OrderStatus.WAIT, sellOrder.getState());
        }
    }

    @Nested
    @DisplayName("지정가 매도주문 생성 테스트")
    class CreateLimitSellOrderTest{
        @DisplayName("orderSize가 null인 지정가 매도주문을 생성시 Exception을 반환함")
        @Test
        public void createLimitSellOrderWithNullOrderSize_throwException(){
            Double nullOrderSize = null;

            assertThrows(DomainValidationException.class, ()->{
                SellOrder.createLimitSellOrder(1L, "BTC", 1, nullOrderSize, 10.0, baseTime, false);
            });
        }

        @DisplayName("price가 null인 지정가 매도주문을 생성시 Exception을 반환함")
        @Test
        public void createLimitSellOrderWithNullPrice_throwException(){
            Double nullPrice = null;

            assertThrows(DomainValidationException.class, ()->{
                SellOrder.createLimitSellOrder(1L, "BTC", 1, 10.0, nullPrice, baseTime, false);
            });
        }

        @DisplayName("orderSize와 price가 null이 아닌 지정가 매도 주문을 생성시 remainingSize 관련 필드가 정상적으로 초기화 됨")
        @Test
        public void createLimitSellOrder_initializeRemainingSizeCorrectly() {
            Double size = 10.0;

            SellOrder sellOrder = SellOrder.createLimitSellOrder(1L, "BTC", 1, size, size, baseTime, false);

            assertEquals(size, sellOrder.getRemainingSize());
        }

        @DisplayName("지정가 매도 주문 생성시 OrderStatus가 WAIT로 초기화 됨")
        @Test
        public void createLimitSellOrder_initializeOrderStatusWithWait() {
            SellOrder sellOrder = SellOrder.createLimitSellOrder(1L, "BTC", 1, 10.0, 10.0, baseTime, false);

            assertEquals(OrderStatus.WAIT, sellOrder.getState());
        }
    }

    @Nested
    @DisplayName("compareTo 테스트")
    class CompareToTest{
        @DisplayName("가격이 작은 지정가 매도주문과 가격이 큰 지정가 매도주문 compareTo시, 가격이 작은 주문이 음수 결과가 나와야 함")
        @Test
        void compareToLimitSellOrdersWithDifferentPrices_smallerSellOrder_returnNegative() {
            SellOrder smallerPriceSellOrder = SellOrder.createLimitSellOrder(1L, "BTC", 1, 100.0, 1.0, baseTime, false);
            SellOrder biggerPriceSellOrder = SellOrder.createLimitSellOrder(2L, "BTC", 1, 100.0, 5.0, baseTime, false);

            assertTrue(smallerPriceSellOrder.compareTo(biggerPriceSellOrder) < 0);
            assertTrue(biggerPriceSellOrder.compareTo(smallerPriceSellOrder) > 0);
        }

        @DisplayName("가격이 동일하고, 생성 시간이 동일한 지정가 매도 주문을 compareTo시, id가 작은 주문이 음수가 나와야 함")
        @Test
        void compareToLimitSellOrderWithSamePricesAndSameTimes_smallerIdSellOrder_returnNegative() {
            SellOrder sameTimeSellOrder1 = SellOrder.createLimitSellOrder(1L, "BTC", 1, 100.0, 1.0, baseTime, false);
            SellOrder sameTimeSellOrder2 = SellOrder.createLimitSellOrder(2L, "BTC", 1, 100.0, 1.0, baseTime, false);

            assertTrue(sameTimeSellOrder1.compareTo(sameTimeSellOrder2) < 0);
        }

        @DisplayName("가격이 같고 생성시간이 다른 지정가 매도주문을 compareTo시 생성시간이 빠른 주문이 음수가 나와야 함")
        @Test
        void compareToLimitSellOrdersWithDifferentTimes_earlierTimeSellOrder_returnNegative() {
            LocalDateTime earlierTime = baseTime.minusSeconds(1);
            LocalDateTime laterTime = baseTime.plusSeconds(1);

            SellOrder earlierTimeSellOrder = SellOrder.createLimitSellOrder(1L, "BTC", 1, 100.0, 1.0, earlierTime, false);
            SellOrder laterTimeSellOrder = SellOrder.createLimitSellOrder(2L, "BTC", 1, 100.0, 1.0, laterTime, false);

            assertTrue(earlierTimeSellOrder.compareTo(laterTimeSellOrder) < 0);
            assertTrue(laterTimeSellOrder.compareTo(earlierTimeSellOrder) > 0);
        }

        @DisplayName("시장가 매도 주문들에 대해 compareTo시 생성시간이 빠른 주문이 음수가 나와야 함")
        @Test
        void compareToMarketSellOrders_earlierTimeSellOrder_returnNegative() {
            LocalDateTime earlierTime = baseTime.minusSeconds(1);
            LocalDateTime laterTime = baseTime.plusSeconds(1);

            SellOrder earlierTimeSellOrder = SellOrder.createMarketSellOrder(1L, "BTC", 1, 100.0, earlierTime, false);
            SellOrder laterTimeSellOrder = SellOrder.createMarketSellOrder(2L, "BTC", 1, 1000.0, laterTime, false);

            assertTrue(earlierTimeSellOrder.compareTo(laterTimeSellOrder) < 0);
            assertTrue(laterTimeSellOrder.compareTo(earlierTimeSellOrder) > 0);
        }
    }
}
