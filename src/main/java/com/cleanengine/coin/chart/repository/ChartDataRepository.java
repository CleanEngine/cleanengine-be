package com.cleanengine.coin.chart.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.repository.query.Param;

public interface ChartDataRepository {
    /**
     * Projection interface for minute-candle aggregation results.
     */
    interface MinuteCandleProjection {
        String getTicker();
        LocalDateTime getBucketStart();
        Double getLowPrice();
        Double getHighPrice();
        Double getOpenPrice();
        Double getClosePrice();
        Double getVolume();
    }

    /**
    시작시간과 끝시간 (1분)의 ohlc 데이터를 반환
     */
    List<MinuteCandleProjection> findMinuteCandles(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * 특정 티커의 캔들 데이터만 조회
     */
    List<MinuteCandleProjection> findMinuteCandlesByTicker(
            @Param("ticker") String ticker,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}