package com.cleanengine.coin.trade.infra;

import com.cleanengine.coin.trade.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findByBuyUserIdAndTicker(Integer buyUserId, String ticker);
    List<Trade> findBySellUserIdAndTicker(Integer sellUserId, String ticker);

}
