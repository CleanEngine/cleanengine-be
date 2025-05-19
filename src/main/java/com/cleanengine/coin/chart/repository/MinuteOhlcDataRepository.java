package com.cleanengine.coin.chart.repository;

import com.cleanengine.coin.trade.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MinuteOhlcDataRepository extends JpaRepository<Trade, Integer> {
    /**
     * ticker로 모든 트레이드를 시간 순으로 조회
     */
    List<Trade> findByTickerOrderByTradeTimeAsc(String ticker);
}