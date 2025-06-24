package com.cleanengine.coin.chart.service.minute;

import com.cleanengine.coin.chart.dto.RealTimeOhlcDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PagingMinuteOhlcDataServiceImpl implements PagingMinuteOhlcDataService {

    private final EntityManager em;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<RealTimeOhlcDto> getMinuteOhlcData(String ticker, int count, int interval, LocalDateTime from) {
        validateTicker(ticker);

        String timeSlotQuery = """
            SELECT DISTINCT DATE_FORMAT(
                DATE_SUB(trade_time, INTERVAL MOD(MINUTE(trade_time), :interval) MINUTE),
                '%Y-%m-%d %H:%i:00') AS time_slot
            FROM trade
            WHERE ticker = :ticker AND trade_time <= :from
            GROUP BY DATE_FORMAT(
                DATE_SUB(trade_time, INTERVAL MOD(MINUTE(trade_time), :interval) MINUTE),
                '%Y-%m-%d %H:%i:00')
            ORDER BY time_slot DESC
            LIMIT :count
            """;

        Query timeSlotNativeQuery = em.createNativeQuery(timeSlotQuery);
        timeSlotNativeQuery.setParameter("ticker", ticker);
        timeSlotNativeQuery.setParameter("from", from);
        timeSlotNativeQuery.setParameter("interval", interval);
        timeSlotNativeQuery.setParameter("count", count);

        @SuppressWarnings("unchecked")
        List<String> timeSlotResults = timeSlotNativeQuery.getResultList();
        List<LocalDateTime> timeSlots = timeSlotResults.stream()
                .map(str -> LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .sorted(Comparator.naturalOrder())
                .toList();

        if (timeSlots.isEmpty()) {
            return Collections.emptyList();
        }

        List<RealTimeOhlcDto> result = new ArrayList<>();

        // 캐시 확인
        for (LocalDateTime timestamp : timeSlots) {
            String cacheKey = String.format("ohlc:min:%d:%s:%s", interval, ticker, timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            RealTimeOhlcDto cachedResult = (RealTimeOhlcDto) redisTemplate.opsForValue().get(cacheKey);
            if (cachedResult != null) {
                result.add(cachedResult);
            }
        }

        // 캐시 미스된 타임슬롯만 조회
        List<LocalDateTime> missingTimeSlots = timeSlots.stream()
                .filter(ts -> result.stream().noneMatch(dto -> dto.getTimestamp().equals(ts)))
                .toList();

        if (!missingTimeSlots.isEmpty()) {
            String ohlcQuery = """
                SELECT
                    :ticker AS ticker,
                    t.time_slot AS timestamp,
                    MAX(CASE WHEN rn_open = 1 THEN price END) AS open,
                    MAX(price) AS high,
                    MIN(price) AS low,
                    MAX(CASE WHEN rn_close = 1 THEN price END) AS close,
                    SUM(size) AS volume
                FROM (
                    SELECT
                        trade_time,
                        price,
                        size,
                        DATE_FORMAT(
                            DATE_SUB(trade_time, INTERVAL MOD(MINUTE(trade_time), :interval) MINUTE),
                            '%Y-%m-%d %H:%i:00') AS time_slot,
                        ROW_NUMBER() OVER (PARTITION BY DATE_FORMAT(
                            DATE_SUB(trade_time, INTERVAL MOD(MINUTE(trade_time), :interval) MINUTE),
                            '%Y-%m-%d %H:%i:00') ORDER BY trade_time) AS rn_open,
                        ROW_NUMBER() OVER (PARTITION BY DATE_FORMAT(
                            DATE_SUB(trade_time, INTERVAL MOD(MINUTE(trade_time), :interval) MINUTE),
                            '%Y-%m-%d %H:%i:00') ORDER BY trade_time DESC) AS rn_close
                    FROM trade
                    WHERE ticker = :ticker
                        AND DATE_FORMAT(
                            DATE_SUB(trade_time, INTERVAL MOD(MINUTE(trade_time), :interval) MINUTE),
                            '%Y-%m-%d %H:%i:00') IN (:timeSlots)
                ) t
                GROUP BY t.time_slot
                ORDER BY t.time_slot
                """;

            Query ohlcNativeQuery = em.createNativeQuery(ohlcQuery);
            ohlcNativeQuery.setParameter("ticker", ticker);
            ohlcNativeQuery.setParameter("interval", interval);
            ohlcNativeQuery.setParameter("timeSlots", missingTimeSlots.stream()
                    .map(ts -> ts.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .toList());

            @SuppressWarnings("unchecked")
            List<Object[]> results = ohlcNativeQuery.getResultList();
            List<RealTimeOhlcDto> ohlcDtos = results.stream()
                    .map(row -> new RealTimeOhlcDto(
                            (String) row[0], // ticker
                            LocalDateTime.parse((String) row[1], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            ((Number) row[2]).doubleValue(), // open
                            ((Number) row[3]).doubleValue(), // high
                            ((Number) row[4]).doubleValue(), // low
                            ((Number) row[5]).doubleValue(), // close
                            ((Number) row[6]).doubleValue()  // volume
                    ))
                    .toList();

            // 개별 캐싱
            for (RealTimeOhlcDto ohlc : ohlcDtos) {
                String cacheKey = String.format("ohlc:min:%d:%s:%s", interval, ticker, ohlc.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                redisTemplate.opsForValue().set(cacheKey, ohlc, 1, TimeUnit.HOURS);
                result.add(ohlc);
            }
        }

        // 결과 정렬
        return result.stream()
                .sorted(Comparator.comparing(RealTimeOhlcDto::getTimestamp))
                .collect(Collectors.toList());
    }

    static void validateTicker(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("티커는 비어있을 수 없습니다");
        }
    }

}
