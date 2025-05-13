package com.cleanengine.coin.chart.service.minute;

import com.cleanengine.coin.chart.dto.RealTimeOhlcDto;
import java.util.List;

public interface MinuteOhlcDataService {
    /**
     * DB에 저장된 Trade 데이터로부터
     * ticker의 과거 1분봉 OHLC+volume 리스트 생성
     */
    List<RealTimeOhlcDto> getMinuteOhlcData(String ticker);
}