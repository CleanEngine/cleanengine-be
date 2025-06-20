package com.cleanengine.coin.chart.service.minute;

import com.cleanengine.coin.chart.dto.RealTimeOhlcDto;

import java.time.LocalDateTime;
import java.util.List;

public interface PagingMinuteOhlcDataService {

    List<RealTimeOhlcDto> getMinuteOhlcData(String ticker, int count, int interval, LocalDateTime from);

}