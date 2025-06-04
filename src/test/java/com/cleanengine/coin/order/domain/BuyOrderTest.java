package com.cleanengine.coin.order.domain;

import com.cleanengine.coin.common.error.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class BuyOrderTest {
    protected static final LocalDateTime baseTime = LocalDateTime.now();

    @Nested
    @DisplayName("시장가 매수주문 생성 테스트")
    class CreateMarketBuyOrderTest{
        @DisplayName("deposit이 null인 시장가 매수주문을 생성시 Exception을 반환함")
        @Test
        public void createMarketBuyOrderWithNullDeposit_throwException(){
            Double nullDeposit = null;

            assertThrows(DomainValidationException.class, ()->{
                BuyOrder.createMarketBuyOrder("BTC", 1, nullDeposit, baseTime, false);
            });
        }

        @DisplayName("deposit이 null이 아닌 시장가 매수 주문을 생성시 deposit 관련 필드가 정상적으로 초기화 됨")
        @Test
        public void createMarketBuyOrderWithDeposit_initializeDepositCorrectly() {
            Double nonNullDeposit = 1000.0;

            BuyOrder buyOrder = BuyOrder.createMarketBuyOrder("BTC", 1, nonNullDeposit, baseTime, false);

            assertEquals(nonNullDeposit, buyOrder.getLockedDeposit());
            assertEquals(nonNullDeposit, buyOrder.getRemainingDeposit());
        }

        @DisplayName("시장가 매수 주문 생성시 OrderStatus가 WAIT로 초기화 됨")
        @Test
        public void createMarketBuyOrder_initializeOrderStatusWithWait() {
            BuyOrder buyOrder = BuyOrder.createMarketBuyOrder("BTC", 1, 1000.0, baseTime, false);

            assertEquals(OrderStatus.WAIT, buyOrder.getState());
        }
    }

    @Nested
    @DisplayName("지정가 매수주문 생성 테스트")
    class CreateLimitBuyOrderTest{
        @DisplayName("orderSize가 null인 지정가 매수주문을 생성시 Exception을 반환함")
        @Test
        public void createLimitBuyOrderWithNullOrderSize_throwException(){
            Double nullOrderSize = null;

            assertThrows(DomainValidationException.class, ()->{
                BuyOrder.createLimitBuyOrder("BTC", 1, nullOrderSize, 100.0, baseTime, false);
            });
        }

        @DisplayName("price가 null인 지정가 매수주문을 생성시 Exception을 반환함")
        @Test
        public void createLimitBuyOrderWithNullPrice_throwException(){
            Double nullPrice = null;

            assertThrows(DomainValidationException.class, ()->{
                BuyOrder.createLimitBuyOrder("BTC", 1, 100.0, nullPrice, baseTime, false);
            });
        }

        @DisplayName("orderSize와 price가 null이 아닌 지정가 매수 주문을 생성시 deposit 관련 필드가 정상적으로 초기화 됨")
        @Test
        public void createLimitBuyOrder_initializeDepositCorrectly() {
            Double orderSize = 10.0;
            Double price = 10.0;

            BuyOrder buyOrder = BuyOrder.createLimitBuyOrder("BTC", 1, orderSize, price, baseTime, false);

            Double deposit = orderSize * price;
            assertEquals(deposit, buyOrder.getLockedDeposit());
            assertEquals(deposit, buyOrder.getRemainingDeposit());
        }

        @DisplayName("지정가 매수 주문 생성시 OrderStatus가 WAIT로 초기화 됨")
        @Test
        public void createLimitBuyOrder_initializeOrderStatusWithWait() {
            BuyOrder buyOrder = BuyOrder.createLimitBuyOrder("BTC", 1, 10.0, 10.0, baseTime, false);

            assertEquals(OrderStatus.WAIT, buyOrder.getState());
        }
    }

    @Nested
    @DisplayName("compareTo 테스트")
    class CompareToTest{
        @DisplayName("가격이 큰 지정가 매수주문과 가격이 작은 지정가 매수주문을 compareTo시, 가격이 큰 주문이 음수 결과가 나와야 함")
        @Test
        void compareToLimitBuyOrdersWithDifferentPrices_biggerBuyOrder_returnNegative() {
            BuyOrder biggerPriceBuyOrder = BuyOrder.createLimitBuyOrder("BTC", 1, 100.0, 5.0, baseTime, false);
            BuyOrder smallerPriceBuyOrder = BuyOrder.createLimitBuyOrder("BTC", 1, 100.0, 1.0, baseTime, false);

            assertTrue(biggerPriceBuyOrder.compareTo(smallerPriceBuyOrder) < 0);
            assertTrue(smallerPriceBuyOrder.compareTo(biggerPriceBuyOrder) > 0);
        }

        @DisplayName("가격이 동일하고, 생성 시간이 동일한 지정가 매수 주문을 compareTo시, 0이 나와야 함")
        @Test
        void compareToLimitBuyOrderWithSamePricesAndSameTimes_returnZero() {
            BuyOrder sameTimeBuyOrder1 = BuyOrder.createLimitBuyOrder("BTC", 1, 100.0, 1.0, baseTime, false);
            BuyOrder sameTimeBuyOrder2 = BuyOrder.createLimitBuyOrder("BTC", 1, 100.0, 1.0, baseTime, false);

            assertEquals(0, sameTimeBuyOrder1.compareTo(sameTimeBuyOrder2));
        }

        @DisplayName("가격이 같고 생성시간이 다른 지정가 매수주문을 compareTo시 생성시간이 빠른 주문이 음수가 나와야 함")
        @Test
        void compareToLimitBuyOrdersWithDifferentTimes_earlierTimeBuyOrder_returnNegative() {
            LocalDateTime earlierTime = baseTime.minusSeconds(1);
            LocalDateTime laterTime = baseTime.plusSeconds(1);

            BuyOrder earlierTimeBuyOrder = BuyOrder.createLimitBuyOrder("BTC", 1, 100.0, 1.0, earlierTime, false);
            BuyOrder laterTimeBuyOrder = BuyOrder.createLimitBuyOrder("BTC", 1, 100.0, 1.0, laterTime, false);

            assertTrue(earlierTimeBuyOrder.compareTo(laterTimeBuyOrder) < 0);
            assertTrue(laterTimeBuyOrder.compareTo(earlierTimeBuyOrder) > 0);
        }

        @DisplayName("시장가 매수 주문들에 대해 compareTo시 생성시간이 빠른 주문이 음수가 나와야 함")
        @Test
        void compareToMarketBuyOrders_earlierTimeBuyOrder_returnNegative() {
            LocalDateTime earlierTime = baseTime.minusSeconds(1);
            LocalDateTime laterTime = baseTime.plusSeconds(1);

            BuyOrder earlierTimeBuyOrder = BuyOrder.createMarketBuyOrder("BTC", 1, 100.0, earlierTime, false);
            BuyOrder laterTimeBuyOrder = BuyOrder.createMarketBuyOrder("BTC", 1, 1000.0, laterTime, false);

            assertTrue(earlierTimeBuyOrder.compareTo(laterTimeBuyOrder) < 0);
            assertTrue(laterTimeBuyOrder.compareTo(earlierTimeBuyOrder) > 0);
        }
    }

    @Nested
    @DisplayName("decreaseRemainingDeposit 테스트")
    class DecreaseRemainingDepositTest{
        @DisplayName("null인 amount로 decreaseRemainingDeposit 호출시, Exception을 반환한다.")
        @Test
        void decreaseRemainingDepositWithNullAmount_throwsException() {
            BuyOrder buyOrder = BuyOrder.createLimitBuyOrder("BTC", 1, 100.0, 10.0, baseTime, false);

            assertThrows(IllegalArgumentException.class, () -> buyOrder.decreaseRemainingDeposit(null));
        }

        @DisplayName("remainingDeposit보다 큰 amount로 decreaseRemainingDeposit 호출시, Exception을 반환한다.")
        @Test
        void decreaseRemainingDepositWithBiggerAmount_throwsException() {
            BuyOrder buyOrder = BuyOrder.createLimitBuyOrder("BTC", 1, 100.0, 10.0, baseTime, false);

            assertThrows(IllegalArgumentException.class, () -> buyOrder.decreaseRemainingDeposit(2000.0));
        }

        @DisplayName("remainingDeposit보다 작은 amount로 decreaseRemainingDeposit 호출시, 정상 적용된다.")
        @Test
        void decreaseRemainingDepositWithSmallerAmount_resultAsExpected() {
            BuyOrder buyOrder = BuyOrder.createLimitBuyOrder("BTC", 1, 100.0, 10.0, baseTime, false);

            buyOrder.decreaseRemainingDeposit(900.0);

            assertEquals(100.0, buyOrder.getRemainingDeposit());
        }
    }

    @Nested
    @DisplayName("decreaseRemainingSize 테스트")
    class DecreaseRemainingSizeTest{
        @DisplayName("시장가 매수 주문에 대해 decreaseRemainingSize를 할 경우 Exception을 반환한다.")
        @Test
        void decreaseRemainingSizeWithMarketBuyOrder_throwsException() {
            BuyOrder buyOrder = BuyOrder.createMarketBuyOrder("BTC", 1, 100.0, baseTime, false);

            assertThrows(IllegalArgumentException.class, () -> buyOrder.decreaseRemainingSize(10.0));
        }
    }
}
