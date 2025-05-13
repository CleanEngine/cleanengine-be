package com.cleanengine.coin.chart.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealTimeOhlcDto {
    private String ticker;
    private LocalDateTime timestamp;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Double volume;
}