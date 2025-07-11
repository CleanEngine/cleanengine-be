package com.cleanengine.coin.realitybot.dto;

import com.querydsl.core.annotations.QueryProjection;

public record BotOrderCount (
        long buyOrderCount,
        long sellOrderCount
){
    @QueryProjection
    public BotOrderCount{}
}
