package com.cleanengine.coin.chart.repository;

import com.cleanengine.coin.trade.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.util.List;


public interface ChartDataRepository extends JpaRepository<Trade, Integer> {
    /**
     * Projection interface for minute-candle aggregation results.
     */
    interface MinuteCandleProjection {
        String getTicker();
        Timestamp getBucketStart();
        Double getOpenPrice();
        Double getHighPrice();
        Double getLowPrice();
        Double getClosePrice();
        Double getVolume();
    }

    /**
     * Fetches 1-minute OHLCV candles by aggregating trade records between start and end.
     *
     * @param start start timestamp (inclusive)
     * @param end   end timestamp (exclusive)
     * @return list of minute-candle projections
     */
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
    List<MinuteCandleProjection> findMinuteCandles(Timestamp start, Timestamp end);
}