package com.cleanengine.coin.trade.adapter.out.persistence;

import com.cleanengine.coin.trade.domain.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaTradeQueryRepository extends JpaRepository<Trade, Integer> {

    List<Trade> findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(String ticker, LocalDateTime startTime, LocalDateTime endTime);

    List<Trade> findByBuyUserIdAndTicker(Integer buyUserId, String ticker);

    List<Trade> findBySellUserIdAndTicker(Integer sellUserId, String ticker);

    List<Trade> findTop10ByTickerOrderByTradeTimeDesc(String ticker);

    List<Trade> findByTickerAndTradeTimeGreaterThanEqualOrderByTradeTimeDesc(String ticker, LocalDateTime lastTime);

    Trade findFirstByTickerOrderByTradeTimeDesc(String ticker);

}
