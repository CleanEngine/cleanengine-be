package com.cleanengine.coin.chart.service;

import com.cleanengine.coin.chart.dto.PrevRateDto;
import com.cleanengine.coin.chart.dto.TradeEventDto;
import com.cleanengine.coin.chart.repository.RealTimeTradeRepository;
import com.cleanengine.coin.trade.entity.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealTimeDataPrevRateService {

    private final RealTimeTradeRepository tradeRepository;

    public PrevRateDto generatePrevRateData(TradeEventDto currentTrade) {
        // 전일 종가 계산
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterdayStart = today.minusDays(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime yesterdayEnd = today.minusDays(1).withHour(23).withMinute(59).withSecond(59);
        log.debug("조회 시간 범위: {} ~ {}", yesterdayStart, yesterdayEnd);
        String ticker = currentTrade.getTicker();
        Trade yesterdayLastTrade = tradeRepository.findFirstByTickerAndTradeTimeBetweenOrderByTradeTimeDesc(
                ticker, yesterdayStart, yesterdayEnd);

        if(yesterdayLastTrade == null){
            log.debug("전일 거래 데이터가 없습니다: {}", ticker);
            return new PrevRateDto(ticker, 0.0, currentTrade.getPrice(), 0.0, LocalDateTime.now());
        }
        double prevClose = yesterdayLastTrade.getPrice();
        double currentPrice = currentTrade.getPrice();
        double changeRate = ((currentPrice - prevClose) / prevClose) * 100;

        return new PrevRateDto(
                ticker,
                prevClose,
                currentPrice,
                changeRate,
                LocalDateTime.now()
        );
    }
}