package com.cleanengine.coin.chart.repository;

import com.cleanengine.coin.trade.entity.Trade;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Profile({"h2", "default"}) // 기본 프로필과 h2 프로필에서 사용
public interface H2ChartDataRepository extends ChartDataRepository, JpaRepository<Trade, Integer> {

    @Override
    @Query(value = """
    SELECT 
        t.ticker AS ticker,
        PARSEDATETIME(FORMATDATETIME(t.trade_time, 'yyyy-MM-dd HH:mm:00'), 'yyyy-MM-dd HH:mm:ss') AS bucket_start,
        MIN(t.price) AS low_price,
        MAX(t.price) AS high_price,
        AVG(t.price) AS open_price,  -- 간소화: 개/종가 대신 평균가 사용
        AVG(t.price) AS close_price, -- 간소화: 개/종가 대신 평균가 사용
        SUM(t.size) AS volume
    FROM trade t
    WHERE t.trade_time >= :start AND t.trade_time < :end
    GROUP BY t.ticker, PARSEDATETIME(FORMATDATETIME(t.trade_time, 'yyyy-MM-dd HH:mm:00'), 'yyyy-MM-dd HH:mm:ss')
    ORDER BY t.ticker, bucket_start
""", nativeQuery = true)
    List<MinuteCandleProjection> findMinuteCandles(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Override
    @Query(value = """
    SELECT 
        t.ticker AS ticker,
        PARSEDATETIME(FORMATDATETIME(t.trade_time, 'yyyy-MM-dd HH:mm:00'), 'yyyy-MM-dd HH:mm:ss') AS bucket_start,
        MIN(t.price) AS low_price,
        MAX(t.price) AS high_price,
        AVG(t.price) AS open_price,  -- 간소화: 개/종가 대신 평균가 사용
        AVG(t.price) AS close_price, -- 간소화: 개/종가 대신 평균가 사용
        SUM(t.size) AS volume
    FROM trade t
    WHERE UPPER(t.ticker) = UPPER(:ticker) AND t.trade_time >= :start AND t.trade_time < :end
    GROUP BY t.ticker, PARSEDATETIME(FORMATDATETIME(t.trade_time, 'yyyy-MM-dd HH:mm:00'), 'yyyy-MM-dd HH:mm:ss')
    ORDER BY bucket_start
""", nativeQuery = true)
    List<MinuteCandleProjection> findMinuteCandlesByTicker(
            @Param("ticker") String ticker,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}