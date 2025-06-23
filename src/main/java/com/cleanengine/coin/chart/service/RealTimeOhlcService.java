package com.cleanengine.coin.chart.service;

import com.cleanengine.coin.chart.dto.RealTimeOhlcDto;
import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.repository.jpaRepository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealTimeOhlcService {

    private final TradeRepository tradeRepository;


    private final Map<String, RealTimeOhlcDto> currentMinuteOhlcCache = new ConcurrentHashMap<>();


    public RealTimeOhlcDto getAndUpdateCumulative1mOhlc(String ticker, LocalDateTime now ) {
        try {
            LocalDateTime currentMinuteStart = now.truncatedTo(ChronoUnit.MINUTES);

            RealTimeOhlcDto cachedOhlc = currentMinuteOhlcCache.get(ticker);

            if (cachedOhlc == null || cachedOhlc.getTimestamp().isBefore(currentMinuteStart)) {
                return handleNewMinute(ticker, now, currentMinuteStart);
            }
            else {
                return handleExistingMinute(ticker, now, cachedOhlc);
            }
        } catch (Exception e) {
            log.error("티커 {}의 누적 OHLC 데이터 생성 중 오류 발생: {}", ticker, e.getMessage(), e);
            // 오류 발생 시 캐시된 마지막 데이터라도 반환
            return currentMinuteOhlcCache.get(ticker);
        }
    }

    private RealTimeOhlcDto handleNewMinute(String ticker, LocalDateTime now, LocalDateTime minuteStart) {
        log.debug("티커 {}: 새로운 1분봉 시작 ({}).", ticker, minuteStart);
        List<Trade> trades = tradeRepository.findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(ticker, minuteStart, now);

        if (trades.isEmpty()) {
            return null;
        }

        // 새 거래내역으로 OHLCV 계산
        CalculateOhlcv ohlcv = getCalculateOhlcv(trades);
        RealTimeOhlcDto newOhlc = createOhlcDto(ticker, now, ohlcv);

        // 캐시를 새로운 1분봉 데이터로 교체
        currentMinuteOhlcCache.put(ticker, newOhlc);
        return newOhlc;
    }


    private RealTimeOhlcDto handleExistingMinute(String ticker, LocalDateTime now, RealTimeOhlcDto cachedOhlc) {
        LocalDateTime lastProcessedTime = cachedOhlc.getTimestamp();
        List<Trade> newTrades = tradeRepository.findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(ticker, lastProcessedTime, now);

        // 새로운 거래가 없다면, 타임스탬프만 최신으로 업데이트하여 "살아있음"을 알림
        if (newTrades.isEmpty()) {
            cachedOhlc.setTimestamp(now);
            return cachedOhlc;
        }

        log.trace("티커 {}: 기존 1분봉 업데이트. 신규 거래 {}건", ticker, newTrades.size());

        // Open(시가)는 분이 끝날때까지 고정
        cachedOhlc.setHigh(Math.max(cachedOhlc.getHigh(), newTrades.stream().mapToDouble(Trade::getPrice).max().orElse(cachedOhlc.getHigh())));
        cachedOhlc.setLow(Math.min(cachedOhlc.getLow(), newTrades.stream().mapToDouble(Trade::getPrice).min().orElse(cachedOhlc.getLow())));
        cachedOhlc.setClose(newTrades.getLast().getPrice()); // 종가는 항상 마지막 거래 가격
        cachedOhlc.setVolume(cachedOhlc.getVolume() + newTrades.stream().mapToDouble(Trade::getSize).sum());
        cachedOhlc.setTimestamp(now); // 마지막 처리 시간 갱신

        return cachedOhlc;
    }



    @NotNull
    private CalculateOhlcv getCalculateOhlcv(List<Trade> trades) {
        Double open = trades.getFirst().getPrice();
        Double close = trades.getLast().getPrice();
        Double high = trades.stream().mapToDouble(Trade::getPrice).max().orElse(0.0);
        Double low = trades.stream().mapToDouble(Trade::getPrice).min().orElse(0.0);
        Double volume = trades.stream().mapToDouble(Trade::getSize).sum();
        return new CalculateOhlcv(open, high, low, close, volume);
    }

    private RealTimeOhlcDto createOhlcDto(String ticker, LocalDateTime timestamp, CalculateOhlcv ohlcv) {
        return new RealTimeOhlcDto(
                ticker, timestamp, ohlcv.open(), ohlcv.high(), ohlcv.low(), ohlcv.close(), ohlcv.volume()
        );
    }

    record CalculateOhlcv(Double open, Double high, Double low, Double close, Double volume) {}
}