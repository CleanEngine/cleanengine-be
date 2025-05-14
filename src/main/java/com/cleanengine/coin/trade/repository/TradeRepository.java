package com.cleanengine.coin.trade.repository;


import com.cleanengine.coin.trade.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
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

}
