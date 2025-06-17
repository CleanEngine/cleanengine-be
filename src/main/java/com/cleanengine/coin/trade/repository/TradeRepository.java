package com.cleanengine.coin.trade.repository;


import com.cleanengine.coin.trade.entity.Trade;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Integer> {

    // 특정 시간 이후의 거래 조회 (페이징 지원)
    List<Trade> findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc
    (
            String ticker,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    List<Trade> findByBuyUserIdAndTicker(Integer buyUserId, String ticker);
    List<Trade> findBySellUserIdAndTicker(Integer sellUserId, String ticker);

    @WithSpan("api.request.02.order.platformvwap.db.1st")
    List<Trade> findTop10ByTickerOrderByTradeTimeDesc(String ticker);
    @WithSpan("api.request.02.order.platformvwap.db.2nd")
    List<Trade> findByTickerAndTradeTimeGreaterThanEqualOrderByTradeTimeDesc(String ticker, LocalDateTime lastTime);
}
