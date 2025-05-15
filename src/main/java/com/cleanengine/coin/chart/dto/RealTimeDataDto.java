package com.cleanengine.coin.chart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RealTimeDataDto {
    private String ticker;
    private double size;
    private double price;
    private double changeRate;
    private LocalDateTime timestamp;
    private String transactionId;
}
