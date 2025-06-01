package com.cleanengine.coin.chart.service;

import com.cleanengine.coin.chart.dto.PrevRateDto;
import com.cleanengine.coin.chart.dto.TradeEventDto;
import com.cleanengine.coin.chart.repository.RealTimeTradeRepository;
import com.cleanengine.coin.trade.entity.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
//todo: 테스트 하기 쉽게 변환하기(메서드 분리)
public class RealTimeDataPrevRateService {

    private final RealTimeTradeRepository tradeRepository;

    public PrevRateDto generatePrevRateData(TradeEventDto currentTrade) {
        // 전일 종가 계산
        String ticker = currentTrade.getTicker();
        LocalDateTime today = LocalDateTime.now();
        YesterDay yesterDay = getYesterDay(today);
        log.debug("조회 시간 범위: {} ~ {}", yesterDay.yesterdayStart(), yesterDay.yesterdayEnd());

        Trade yesterdayLastTrade = tradeRepository.findFirstByTickerAndTradeTimeBetweenOrderByTradeTimeDesc(
                ticker, yesterDay.yesterdayStart(), yesterDay.yesterdayEnd());

        if (yesterdayLastTrade == null) {
            log.debug("전일 거래 데이터가 없습니다: {}", ticker);
            return new PrevRateDto(ticker, 0.0, currentTrade.getPrice(), 0.0, LocalDateTime.now());
        }
        double prevClose = yesterdayLastTrade.getPrice();
        double currentPrice = currentTrade.getPrice();
        double changeRate = getChangeRate(currentPrice, prevClose);

        return new PrevRateDto(
                ticker,
                prevClose,
                currentPrice,
                changeRate,
                today
        );
    }

    private static double getChangeRate(double currentPrice, double prevClose) {
        double changeRate = ((currentPrice - prevClose) / prevClose) * 100;
        return changeRate;
    }

    //시간 데이터는 파라미터로 주입받아서 활용받는게 좋음(test관점)
    @NotNull
    private static YesterDay getYesterDay(LocalDateTime today) {
        LocalDateTime yesterdayStart = today.minusDays(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime yesterdayEnd = today.minusDays(1).withHour(23).withMinute(59).withSecond(59);
        YesterDay result = new YesterDay(yesterdayStart, yesterdayEnd);
        return result;
    }

    private record YesterDay(LocalDateTime yesterdayStart, LocalDateTime yesterdayEnd) {
    }
}