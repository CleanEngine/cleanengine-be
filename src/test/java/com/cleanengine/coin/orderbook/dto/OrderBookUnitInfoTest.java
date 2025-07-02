package com.cleanengine.coin.orderbook.dto;

import com.cleanengine.coin.orderbook.domain.BuyOrderBookUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrderBookUnitInfoTest {

    @Nested
    @DisplayName("기본 생성 유효성 테스트")
    class CreateOrderBookUnitInfoTest {
        @DisplayName("price가 null이라면 생성시 IllegalArgumentException을 반환한다.")
        @Test
        public void createOrderBookUnitInfoWithNullPrice_throwsIllegalArgumentException() {
            Double nullPrice = null;

            assertThrows(IllegalArgumentException.class, () -> new OrderBookUnitInfo(nullPrice, 1.0, 1.0));
        }

        @DisplayName("size가 null이라면 생성시 IllegalArgumentException을 반환한다.")
        @Test
        public void createOrderBookUnitInfoWithNullSize_throwsIllegalArgumentException() {
            Double nullSize = null;

            assertThrows(IllegalArgumentException.class, () -> new OrderBookUnitInfo(1.0, nullSize, 1.0));
        }

        @DisplayName("priceChangePercent가 null이라면 생성시 IllegalArgumentException을 반환한다.")
        @Test
        public void createOrderBookUnitInfoWithNullPriceChangePercent_throwsIllegalArgumentException() {
            Double nullPriceChangePercent = null;

            assertThrows(IllegalArgumentException.class, () -> new OrderBookUnitInfo(1.0, 1.0, nullPriceChangePercent));
        }
    }

    @Nested
    @DisplayName("priceChangePercent 반영 테스트")
    class ChangePercentTest {
        @DisplayName("closingPrice가 0이하일 경우 percentChange도 0이다.")
        @Test
        public void createOrderBookUnitInfoWithZeroClosingPrice_percentChangeIsZero() {
            Double closingPrice = 0.0;

            OrderBookUnitInfo orderBookUnitInfo = new OrderBookUnitInfo(1.0, 1.0, closingPrice);

            assertEquals(0.0, orderBookUnitInfo.priceChangePercent());
        }

        @DisplayName("closingPrice가 null이라면 IllegalArgumentException을 반환한다.")
        @Test
        public void createOrderBookUnitInfoWithNullClosingPrice_throwsIllegalArgumentException() {
            Double nullClosingPrice = null;
            BuyOrderBookUnit buyOrderBookUnit = new BuyOrderBookUnit(1.0, 1.0);

            assertThrows(IllegalArgumentException.class, () -> new OrderBookUnitInfo(buyOrderBookUnit, nullClosingPrice));
        }

        @DisplayName("price가 null이라면 IllegalArgumentException을 반환다.")
        @Test
        public void createOrderBookUnitInfoWithNullPrice_throwsIllegalArgumentException() {
            Double nullPrice = null;
            BuyOrderBookUnit buyOrderBookUnit = new BuyOrderBookUnit(nullPrice, 1.0);

            assertThrows(IllegalArgumentException.class, () -> new OrderBookUnitInfo(buyOrderBookUnit, 1.0));
        }

        @DisplayName("closingPrice가 비교대상 price의 절반이라면, percentChange는 +100.0이다.")
        @Test
        public void createOrderBookUnitInfoWithHalfClosingPrice_percentChangeIs100() {
            Double closingPrice = 1.0;
            BuyOrderBookUnit buyOrderBookUnit = new BuyOrderBookUnit(closingPrice * 2.0, 1.0);

            OrderBookUnitInfo orderBookUnitInfo = new OrderBookUnitInfo(buyOrderBookUnit, closingPrice);

            assertEquals(100.0, orderBookUnitInfo.priceChangePercent());
        }

        @DisplayName("closingPrice가 비교대상 price의 두배라면, percentChange는 -50.0이다.")
        @Test
        public void createOrderBookUnitInfoWithDoubleClosingPrice_percentChangeIsMinus50() {
            Double closingPrice = 1.0;
            BuyOrderBookUnit buyOrderBookUnit = new BuyOrderBookUnit(closingPrice / 2.0, 1.0);

            OrderBookUnitInfo orderBookUnitInfo = new OrderBookUnitInfo(buyOrderBookUnit, closingPrice);

            assertEquals(-50.0, orderBookUnitInfo.priceChangePercent());
        }
    }
}
