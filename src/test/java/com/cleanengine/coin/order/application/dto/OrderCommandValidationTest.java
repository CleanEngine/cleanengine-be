package com.cleanengine.coin.order.application.dto;

import com.cleanengine.coin.base.ValidatorTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderCommandValidationTest {
    @Nested
    class CreateOrderValidationTest extends ValidatorTest<OrderCommand.CreateOrder> {
        @DisplayName("길이가 10이 넘는 ticker를 가진 CreateOrder 검증시 Exception을 반환함")
        @Test
        void validateCreateOrderWithLongTickerName_returnsException() {
            String longTicker = "a".repeat(11);

            OrderCommand.CreateOrder createOrder = new OrderCommand.CreateOrder(
                    longTicker, 3, true, false,
                    30.0, 50.0, LocalDateTime.now(), false);

            List<ConstraintViolationInfo> constraintViolationInfos = validate(createOrder);
            assertEquals(1, constraintViolationInfos.size());

            ConstraintViolationInfo violationInfo = constraintViolationInfos.get(0);
            assertEquals("ticker", violationInfo.getFieldName());
            assertEquals(longTicker, violationInfo.getInvalidValue());
        }

        @DisplayName("길이가 0인 ticker를 가진 CreateOrder 검증시 Exception을 반환함")
        @Test
        void validateCreateOrderWithEmptyTickerName_returnsException() {
            String emptyTicker = "";

            OrderCommand.CreateOrder createOrder = new OrderCommand.CreateOrder(
                    emptyTicker, 3, true, false,
                    30.0, 50.0, LocalDateTime.now(), false);

            List<ConstraintViolationInfo> constraintViolationInfos = validate(createOrder);
            assertEquals(1, constraintViolationInfos.size());

            ConstraintViolationInfo violationInfo = constraintViolationInfos.get(0);
            assertEquals("ticker", violationInfo.getFieldName());
            assertEquals(emptyTicker, violationInfo.getInvalidValue());
        }

        @DisplayName("orderSize가 0인 CreateOrder 검증시 Exception을 반환함")
        @Test
        void validateCreateOrderWithZeroOrderSize_returnsException() {
            Double zeroOrderSize = 0.0;

            OrderCommand.CreateOrder createOrder = new OrderCommand.CreateOrder(
                    "BTC", 3, true, false,
                    zeroOrderSize, 50.0, LocalDateTime.now(), false);

            List<ConstraintViolationInfo> constraintViolationInfos = validate(createOrder);
            assertEquals(1, constraintViolationInfos.size());

            ConstraintViolationInfo violationInfo = constraintViolationInfos.get(0);
            assertEquals("orderSize", violationInfo.getFieldName());
            assertEquals(zeroOrderSize, violationInfo.getInvalidValue());
        }

        @DisplayName("price가 0인 CreateOrder 검증시 Exception을 반환함")
        @Test
        void validateCreateOrderWithZeroPrice_returnsException() {
            Double zeroPrice = 0.0;

            OrderCommand.CreateOrder createOrder = new OrderCommand.CreateOrder(
                    "BTC", 3, true, false,
                    50.0, zeroPrice, LocalDateTime.now(), false);

            List<ConstraintViolationInfo> constraintViolationInfos = validate(createOrder);
            assertEquals(1, constraintViolationInfos.size());

            ConstraintViolationInfo violationInfo = constraintViolationInfos.get(0);
            assertEquals("price", violationInfo.getFieldName());
            assertEquals(zeroPrice, violationInfo.getInvalidValue());
        }
    }
}
