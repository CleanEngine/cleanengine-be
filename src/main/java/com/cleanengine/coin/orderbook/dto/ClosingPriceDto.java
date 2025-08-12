package com.cleanengine.coin.orderbook.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDate;

public record ClosingPriceDto(String ticker, LocalDate baseDate, Double closingPrice) {
    @QueryProjection
    public ClosingPriceDto {}
}
