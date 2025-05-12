package com.cleanengine.coin.order.application;

import com.cleanengine.coin.common.validation.ConstraintMessageTemplate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class OrderCommand {
    private OrderCommand() {}

    public record CreateOrder(
            @Size(min = 1, max = 10, message = "ticker" + ConstraintMessageTemplate.SIZE_MESSAGE_TEMPLATE)
            @NotNull(message = "ticker" + ConstraintMessageTemplate.NOTNULL_MESSAGE_TEMPLATE)
            String ticker,

            @NotNull(message = "userId" + ConstraintMessageTemplate.NOTNULL_MESSAGE_TEMPLATE)
            Integer userId,

            @NotNull(message = "isBuyOrder" + ConstraintMessageTemplate.NOTNULL_MESSAGE_TEMPLATE)
            Boolean isBuyOrder,

            @NotNull(message = "isMarketOrder" + ConstraintMessageTemplate.NOTNULL_MESSAGE_TEMPLATE)
            Boolean isMarketOrder,

            //TODO 시장가 매수 주문 여부에 따라 Cosntraint 달라져야 한다.
            @Positive(message = "orderSize" + ConstraintMessageTemplate.POSITIVE_MESSAGE_TEMPLATE)
            Double orderSize,

            @Positive(message = "price" + ConstraintMessageTemplate.POSITIVE_MESSAGE_TEMPLATE)
            Double price,

            @NotNull(message = "createdAt" + ConstraintMessageTemplate.NOTNULL_MESSAGE_TEMPLATE)
            LocalDateTime createdAt,

            @NotNull(message = "isBot" + ConstraintMessageTemplate.NOTNULL_MESSAGE_TEMPLATE)
            Boolean isBot
    ){ }
}

