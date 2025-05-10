package com.cleanengine.coin.chart.repository;


import com.cleanengine.coin.trade.entity.Trade;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
@Profile({"h2", "default"}) // 기본 프로필과 h2 프로필에서 사용
public interface H2ChartDataRepository extends ChartDataRepository, JpaRepository<Trade, Integer> {

    @Override
    @Query(value = """
        WITH cte AS (
          SELECT
            t.ticker,
            PARSEDATETIME(FORMATDATETIME(t.trade_time, 'yyyy-MM-dd HH:mm:00'), 'yyyy-MM-dd HH:mm:ss') AS bucket_start,
            t.price,
            t.size,
            ROW_NUMBER() OVER (
              PARTITION BY t.ticker,
                           PARSEDATETIME(FORMATDATETIME(t.trade_time, 'yyyy-MM-dd HH:mm:00'), 'yyyy-MM-dd HH:mm:ss')
              ORDER BY t.trade_time ASC
            ) AS rn_open,
            ROW_NUMBER() OVER (
              PARTITION BY t.ticker,
                           PARSEDATETIME(FORMATDATETIME(t.trade_time, 'yyyy-MM-dd HH:mm:00'), 'yyyy-MM-dd HH:mm:ss')
              ORDER BY t.trade_time DESC
            ) AS rn_close
          FROM trade t
          WHERE t.trade_time >= :start
            AND t.trade_time <  :end
        )
        SELECT
          ticker,
          bucket_start,
          MAX(CASE WHEN rn_open  = 1 THEN price END) AS open_price,
          MAX(price)                                 AS high_price,
          MIN(price)                                 AS low_price,
          MAX(CASE WHEN rn_close = 1 THEN price END) AS close_price,
          SUM(size)                                  AS volume
        FROM cte
        GROUP BY ticker, bucket_start
        ORDER BY ticker, bucket_start
    """, nativeQuery = true)
    List<MinuteCandleProjection> findMinuteCandles(@Param("start") Timestamp start, @Param("end") Timestamp end);
}