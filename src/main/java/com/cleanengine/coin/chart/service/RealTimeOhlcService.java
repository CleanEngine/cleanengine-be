package com.cleanengine.coin.chart.service;

import com.cleanengine.coin.chart.dto.RealTimeOhlcDto;
import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealTimeOhlcService {

    private final TradeRepository tradeRepository;
    private final Map<String, RealTimeOhlcDto> currentMinuteOhlcCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastProcessedTime = new ConcurrentHashMap<>();

    public RealTimeOhlcDto getAndUpdateCumulative1mOhlc(String ticker, LocalDateTime now) {
        LocalDateTime minuteStart = now.truncatedTo(ChronoUnit.MINUTES);
        RealTimeOhlcDto cached = currentMinuteOhlcCache.get(ticker);

        if (cached == null || cached.getTimestamp().isBefore(minuteStart)) {
            return handleNewMinute(ticker, minuteStart, now);
        } else {
            return handleExistingMinute(ticker, now, cached);
        }
    }

    private RealTimeOhlcDto handleNewMinute(String ticker, LocalDateTime minuteStart, LocalDateTime now) {
        log.debug("티커 {}: 새로운 분 시작 ({}).", ticker, minuteStart);
        List<Trade> trades = tradeRepository.findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(ticker, minuteStart, now);

        double open, high, low, close, volume;
        if (trades.isEmpty()) {
            // 새 분이 시작되었지만 거래가 아직 없는 경우
            // 이전 분의 종가를 임시 값으로 사용하되, open은 0.0으로 설정하여 아직 확정되지 않았음을 표시
            RealTimeOhlcDto prev = currentMinuteOhlcCache.get(ticker);
            double lastClose = (prev != null) ? prev.getClose() : 0.0;
            open = 0.0; // 시가는 아직 미정
            high = low = close = lastClose;
            volume = 0.0;
        } else {
            // 거래가 있으면 정상적으로 OHLC 계산
            open = trades.get(0).getPrice();
            close = trades.get(trades.size() - 1).getPrice();
            high = trades.stream().mapToDouble(Trade::getPrice).max().orElse(open);
            low = trades.stream().mapToDouble(Trade::getPrice).min().orElse(open);
            volume = trades.stream().mapToDouble(Trade::getSize).sum();
        }

        RealTimeOhlcDto dto = new RealTimeOhlcDto(ticker, minuteStart, open, high, low, close, volume);
        currentMinuteOhlcCache.put(ticker, dto);
        lastProcessedTime.put(ticker, now);
        return dto;
    }

    private RealTimeOhlcDto handleExistingMinute(String ticker, LocalDateTime now, RealTimeOhlcDto cached) {
        LocalDateTime lastTime = lastProcessedTime.getOrDefault(ticker, cached.getTimestamp());
        List<Trade> newTrades = tradeRepository.findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(ticker, lastTime, now);

        if (!newTrades.isEmpty()) {
            // [수정된 부분] 만약 시가가 아직 설정되지 않았다면(0.0), 새로운 거래의 첫 가격으로 시가를 설정
            if (cached.getOpen() == 0.0) {
                cached.setOpen(newTrades.get(0).getPrice());
                // 시가가 처음 정해지므로, 고가/저가도 이 가격으로 초기화
                cached.setHigh(newTrades.get(0).getPrice());
                cached.setLow(newTrades.get(0).getPrice());
            }

            double maxPrice = newTrades.stream().mapToDouble(Trade::getPrice).max().orElse(cached.getHigh());
            double minPrice = newTrades.stream().mapToDouble(Trade::getPrice).min().orElse(cached.getLow());
            double lastPrice = newTrades.get(newTrades.size() - 1).getPrice();
            double addedVol = newTrades.stream().mapToDouble(Trade::getSize).sum();

            cached.setHigh(Math.max(cached.getHigh(), maxPrice));
            cached.setLow(Math.min(cached.getLow(), minPrice));
            cached.setClose(lastPrice);
            cached.setVolume(cached.getVolume() + addedVol);
            lastProcessedTime.put(ticker, now);
        }
        return cached;
    }

    @Scheduled(fixedRate = 300000)
    public void cleanupOldCache() {
        LocalDateTime minuteStart = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Iterator<Map.Entry<String, RealTimeOhlcDto>> iter = currentMinuteOhlcCache.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, RealTimeOhlcDto> entry = iter.next();
            if (entry.getValue().getTimestamp().isBefore(minuteStart)) {
                iter.remove();
                lastProcessedTime.remove(entry.getKey());
                log.debug("오래된 OHLC 캐시 삭제: {}", entry.getKey());
            }
        }
    }

    public Map<String, RealTimeOhlcDto> getCurrentCacheStatus() {
        return Map.copyOf(currentMinuteOhlcCache);
    }
}