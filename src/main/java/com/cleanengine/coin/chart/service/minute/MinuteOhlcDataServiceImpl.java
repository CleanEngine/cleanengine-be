package com.cleanengine.coin.chart.service.minute;

import com.cleanengine.coin.chart.dto.RealTimeOhlcDto;
import com.cleanengine.coin.chart.repository.MinuteOhlcDataRepository;
import com.cleanengine.coin.trade.entity.Trade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MinuteOhlcDataServiceImpl implements MinuteOhlcDataService {

    private final MinuteOhlcDataRepository tradeRepository;

    @Override
    public List<RealTimeOhlcDto> getMinuteOhlcData(String ticker) {
        // 1) 해당 티커의 모든 트레이드를 시간 순으로 조회
        List<Trade> trades = tradeRepository.findByTickerOrderByTradeTimeAsc(ticker);

        // 2) 분 단위로 그룹핑 (tradeTime 을 분 단위로 자르고 순서 유지)
        Map<LocalDateTime, List<Trade>> byMinute = trades.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTradeTime().truncatedTo(ChronoUnit.MINUTES),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // 3) 각 분 그룹마다 OHLC + **거래량(volume)** 계산
        return byMinute.entrySet().stream()
                .map(entry -> {
                    LocalDateTime minute = entry.getKey();
                    List<Trade> bucket = entry.getValue();

                    double open  = bucket.get(0).getPrice();
                    double close = bucket.get(bucket.size() - 1).getPrice();
                    double high  = bucket.stream()
                            .mapToDouble(Trade::getPrice)
                            .max()
                            .orElse(open);
                    double low   = bucket.stream()
                            .mapToDouble(Trade::getPrice)
                            .min()
                            .orElse(open);

                    // ← 여기를 바꿔서 “거래량”을 size 필드의 합으로 계산
                    double volume = bucket.stream()
                            .mapToDouble(Trade::getSize)
                            .sum();

                    return new RealTimeOhlcDto(
                            ticker,
                            minute,
                            open,
                            high,
                            low,
                            close,
                            volume
                    );
                })
                .collect(Collectors.toList());
    }
}