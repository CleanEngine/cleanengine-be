package com.cleanengine.coin.realitybot.dto;

import com.querydsl.core.annotations.QueryProjection;

public record BotOrderInfo(
        int userId,
        String ticker,
        long orderId
) {
    @QueryProjection
    public BotOrderInfo{}
}
