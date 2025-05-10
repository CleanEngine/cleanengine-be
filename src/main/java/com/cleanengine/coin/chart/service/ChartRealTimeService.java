package com.cleanengine.coin.chart.service;


import com.cleanengine.coin.chart.Dto.RealTimeTradeDto;

public interface ChartRealTimeService
{
    RealTimeTradeDto getRealTimeTrade(String ticker);
}
