package com.cleanengine.coin.chart.service;

import com.cleanengine.coin.chart.dto.RealTimeOhlcDto;
import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            // 현재 시간
            LocalDateTime now = LocalDateTime.now();

            // 마지막 처리 시간 (없으면 현재 시간에서 1초 전)
            LocalDateTime lastProcessedTime = lastProcessedTimeMap.getOrDefault(
                    ticker, now.minusSeconds(1));

            // 1초 전부터 현재까지의 데이터 조회
            List<Trade> recentTrades = tradeRepository.findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(
                    ticker,
                    lastProcessedTime,
                    now
            );

            // 시간 순서대로 정렬이 필요하면 뒤집음
            Collections.reverse(recentTrades);

            // 거래 데이터가 없으면 마지막으로 캐싱된 데이터 반환
            if (recentTrades.isEmpty()) {
                return lastOhlcDataMap.getOrDefault(ticker, null);
            }

            // 새로운 마지막 처리 시간 업데이트
            lastProcessedTimeMap.put(ticker, now);

            // OHLC 계산
            Double open = recentTrades.get(0).getPrice();
            Double high = recentTrades.stream().mapToDouble(Trade::getPrice).max().orElse(0.0);
            Double low = recentTrades.stream().mapToDouble(Trade::getPrice).min().orElse(0.0);
            Double close = recentTrades.get(recentTrades.size() - 1).getPrice();
            Double volume = recentTrades.stream().mapToDouble(Trade::getSize).sum();

            // RealTimeOhlcDto 생성
            RealTimeOhlcDto ohlcData = new RealTimeOhlcDto(
                    ticker,
                    now,
                    open,
                    high,
                    low,
                    close,
                    volume
            );

            // 캐시에 저장
            lastOhlcDataMap.put(ticker, ohlcData);

            return ohlcData;
        } catch (Exception e) {
            log.error("실시간 OHLC 데이터 생성 중 오류: {}", e.getMessage(), e);
            return lastOhlcDataMap.getOrDefault(ticker, null);
        }
    }
}