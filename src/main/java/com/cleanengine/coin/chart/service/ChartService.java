package com.cleanengine.coin.chart.service;


import com.cleanengine.coin.chart.Dto.CandleDto;
import com.cleanengine.coin.chart.Dto.RealTimeTradeDto;
import com.cleanengine.coin.chart.repository.ChartDataRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChartService {
    List<CandleDto> getMinuteCandles(LocalDateTime start, LocalDateTime end);


    //todo: 시간 단위로도 가능하게
    List<CandleDto> getHourCandles(LocalDateTime start, LocalDateTime end);


    private RealTimeTradeDto convertToRealTimeTradeDto() {
        return new RealTimeTradeDto();
    }
}

