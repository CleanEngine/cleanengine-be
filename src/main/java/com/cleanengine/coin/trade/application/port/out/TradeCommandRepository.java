package com.cleanengine.coin.trade.application.port.out;

import com.cleanengine.coin.trade.domain.model.Trade;

import java.util.List;

public interface TradeCommandRepository {

    <S extends Trade> List<S> saveAll(Iterable<S> entities);

    Trade save(Trade trade);

    void deleteAll();

}
