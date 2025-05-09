package com.cleanengine.coin.trade.infra;

import com.cleanengine.coin.trade.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, Long> {
}
