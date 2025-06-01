package com.cleanengine.coin.chart.service;

import com.cleanengine.coin.chart.dto.RealTimeOhlcDto;
import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealTimeOhlcService {

    private final TradeRepository tradeRepository;

    // 티커별 마지막 처리 시간
    private final Map<String, LocalDateTime> lastProcessedTimeMap = new ConcurrentHashMap<>();

    // 티커별 마지막 OHLC 데이터 캐싱
    private final Map<String, RealTimeOhlcDto> lastOhlcDataMap = new ConcurrentHashMap<>();

    /**
     * 특정 티커의 최신 1초 OHLC 데이터 생성
     */
    public RealTimeOhlcDto getRealTimeOhlc(String ticker) {
        try {
            LocalDateTime now = LocalDateTime.now();

            // 시간 범위 계산
            TimeRange timeRange = calculateTimeRange(ticker, now);

            // 거래 데이터 조회 및 전처리
            List<Trade> recentTrades = getProcessedTradeData(ticker, timeRange);

            // 거래 데이터가 없으면 캐시된 데이터 반환
            if (recentTrades.isEmpty()) {
                return getCachedData(ticker);
            }

            calculateOhlcv ohlcv = getCalculateOhlcv(recentTrades);

            RealTimeOhlcDto ohlcData = createOhlcDto(ticker, now, ohlcv);

            // 캐시 업데이트
            updateCache(ticker, now, ohlcData);

            return ohlcData;
        } catch (Exception e) {
            log.error("실시간 OHLC 데이터 생성 중 오류: {}", e.getMessage(), e);
            return getCachedData(ticker);
        }
    }

    // 시간 범위 계산
    TimeRange calculateTimeRange(String ticker, LocalDateTime now) {
        LocalDateTime lastProcessedTime = lastProcessedTimeMap.getOrDefault(
                ticker, now.minusSeconds(1));
        return new TimeRange(lastProcessedTime, now);
    }

    // 거래 데이터 조회 및 전처리
    List<Trade> getProcessedTradeData(String ticker, TimeRange timeRange) {
        List<Trade> recentTrades = tradeRepository.findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(
                ticker,
                timeRange.start(),
                timeRange.end()
        );

        Collections.reverse(recentTrades);
        return recentTrades;
    }

    // 캐시 업데이트
    void updateCache(String ticker, LocalDateTime now, RealTimeOhlcDto ohlcData) {
        lastProcessedTimeMap.put(ticker, now);
        lastOhlcDataMap.put(ticker, ohlcData);
    }

    // 캐시된 데이터 조회
    RealTimeOhlcDto getCachedData(String ticker) {
        return lastOhlcDataMap.getOrDefault(ticker, null);
    }

    // DTO 생성
    RealTimeOhlcDto createOhlcDto(String ticker, LocalDateTime timestamp, calculateOhlcv ohlcv) {
        return new RealTimeOhlcDto(
                ticker,
                timestamp,
                ohlcv.open(),
                ohlcv.high(),
                ohlcv.low(),
                ohlcv.close(),
                ohlcv.volume()
        );
    }

    // OHLCV 계산 메서드
    @NotNull
    static calculateOhlcv getCalculateOhlcv(List<Trade> recentTrades) {
        Double open = recentTrades.get(0).getPrice();
        Double high = recentTrades.stream().mapToDouble(Trade::getPrice).max().orElse(0.0);
        Double low = recentTrades.stream().mapToDouble(Trade::getPrice).min().orElse(0.0);
        Double close = recentTrades.get(recentTrades.size() - 1).getPrice();
        Double volume = recentTrades.stream().mapToDouble(Trade::getSize).sum();
        return new calculateOhlcv(open, high, low, close, volume);
    }

    record TimeRange(LocalDateTime start, LocalDateTime end) {}

    record calculateOhlcv(Double open, Double high, Double low, Double close, Double volume) {}
}