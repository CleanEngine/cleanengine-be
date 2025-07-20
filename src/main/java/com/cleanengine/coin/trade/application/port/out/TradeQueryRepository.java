package com.cleanengine.coin.trade.application.port.out;

import com.cleanengine.coin.orderbook.dto.ClosingPriceDto;
import com.cleanengine.coin.trade.domain.model.Trade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TradeQueryRepository {

    // 특정 시간 이후의 거래 조회 (페이징 지원)
    List<Trade> findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(String ticker, LocalDateTime startTime, LocalDateTime endTime);

    List<Trade> findByBuyUserIdAndTicker(Integer buyUserId, String ticker);

    List<Trade> findBySellUserIdAndTicker(Integer sellUserId, String ticker);

    List<Trade> findTop10ByTickerOrderByTradeTimeDesc(String ticker);

    List<Trade> findByTickerAndTradeTimeGreaterThanEqualOrderByTradeTimeDesc(String ticker, LocalDateTime lastTime);

    Trade findFirstByTickerOrderByTradeTimeDesc(String ticker);

    ClosingPriceDto getYesterdayClosingPrice(String ticker, LocalDate yesterdayDate);

    long count();

}
