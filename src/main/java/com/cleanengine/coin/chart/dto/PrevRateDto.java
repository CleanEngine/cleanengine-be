package com.cleanengine.coin.chart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrevRateDto {
    private String ticker;
    private double prevClose;
    private double currentPrice;
    private double changeRate;
    private LocalDateTime timestamp;
}