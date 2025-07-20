package com.cleanengine.coin.trade.adapter.out.persistence;

import com.cleanengine.coin.trade.application.port.out.TradeCommandRepository;
import com.cleanengine.coin.trade.domain.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTradeCommandRepository extends JpaRepository<Trade, Integer>, TradeCommandRepository {

}
