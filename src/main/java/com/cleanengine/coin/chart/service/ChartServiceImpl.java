package com.cleanengine.coin.chart.service;

import com.cleanengine.coin.chart.Dto.CandleDto;
import com.cleanengine.coin.chart.repository.ChartDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChartServiceImpl implements ChartService{

    private final ChartDataRepository chartDataRepository;

    @Override
    public List<CandleDto> getMinuteCandles(LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching candle data from {} to {}", start, end);

        try {
            List<ChartDataRepository.MinuteCandleProjection> projections =
                    chartDataRepository.findMinuteCandles(
                            Timestamp.valueOf(start),
                            Timestamp.valueOf(end)
                    );

            log.debug("Found {} candles", projections.size());

            return projections.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching minute candles", e);
            return List.of(); // 빈 목록 반환
        }
    }


    //todo: 시간 단위로도 가능하게
    @Override
    public List<CandleDto> getHourCandles(LocalDateTime start, LocalDateTime end) {
        return List.of();
    }


    private CandleDto convertToDto(ChartDataRepository.MinuteCandleProjection projection) {
        return new CandleDto(
                projection.getTicker(),
                projection.getBucketStart().toLocalDateTime(),
                projection.getOpenPrice(),
                projection.getHighPrice(),
                projection.getLowPrice(),
                projection.getClosePrice(),
                projection.getVolume()
        );
    }
}