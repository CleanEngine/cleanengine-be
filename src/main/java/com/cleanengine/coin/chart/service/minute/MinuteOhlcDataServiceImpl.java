package com.cleanengine.coin.chart.service.minute;

import com.cleanengine.coin.chart.dto.RealTimeOhlcDto;
import com.cleanengine.coin.chart.repository.MinuteOhlcDataRepository;
import com.cleanengine.coin.trade.domain.model.Trade;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MinuteOhlcDataServiceImpl implements MinuteOhlcDataService {

    private final MinuteOhlcDataRepository tradeRepository;

    @Override
    public List<RealTimeOhlcDto> getMinuteOhlcData(String ticker) {
        validateTicker(ticker);

        List<Trade> trades = getTradeData(ticker);

        if (trades.isEmpty()) {
            return List.of();
        }

        Map<LocalDateTime, List<Trade>> groupedByMinute = groupTradesByMinute(trades);

        return convertToOhlcData(ticker, groupedByMinute);
    }

    // 입력 검증
    void validateTicker(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("티커는 비어있을 수 없습니다");
        }
    }

    // 거래 데이터 조회
    List<Trade> getTradeData(String ticker) {
        return tradeRepository.findByTickerOrderByTradeTimeAsc(ticker);
    }

    // 분 단위 그룹핑 로직
    Map<LocalDateTime, List<Trade>> groupTradesByMinute(List<Trade> trades) {
        return trades.stream()
                .collect(Collectors.groupingBy(
                        this::truncateToMinute,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }


    LocalDateTime truncateToMinute(Trade trade) {
        return trade.getTradeTime().truncatedTo(ChronoUnit.MINUTES);
    }

    // OHLC 데이터 변환 (메인 비즈니스 로직)
    List<RealTimeOhlcDto> convertToOhlcData(String ticker, Map<LocalDateTime, List<Trade>> groupedByMinute) {
        return groupedByMinute.entrySet().stream()
                .map(entry -> createOhlcDto(ticker, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    RealTimeOhlcDto createOhlcDto(String ticker, LocalDateTime minute, List<Trade> trades) {
        validateTradeList(trades);

        OhlcData ohlcData = calculateOhlcData(trades);

        return new RealTimeOhlcDto(
                ticker,
                minute,
                ohlcData.open(),
                ohlcData.high(),
                ohlcData.low(),
                ohlcData.close(),
                ohlcData.volume()
        );
    }

    void validateTradeList(List<Trade> trades) {
        if (trades == null || trades.isEmpty()) {
            throw new IllegalArgumentException("거래 데이터가 없습니다");
        }
    }

    // OHLC 계산 로직
    @NotNull
    static OhlcData calculateOhlcData(List<Trade> trades) {
        double open = trades.getFirst().getPrice();
        double close = trades.getLast().getPrice();

        double high = trades.stream()
                .mapToDouble(Trade::getPrice)
                .max()
                .orElse(open);

        double low = trades.stream()
                .mapToDouble(Trade::getPrice)
                .min()
                .orElse(open);

        double volume = trades.stream()
                .mapToDouble(Trade::getSize)
                .sum();

        return new OhlcData(open, high, low, close, volume);
    }

    // OHLC 데이터를 위한 레코드 (불변 객체)
    record OhlcData(double open, double high, double low, double close, double volume) {}
}