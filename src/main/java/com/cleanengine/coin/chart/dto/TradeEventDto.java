package com.cleanengine.coin.chart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeEventDto {
    private String ticker;        // 종목 코드
    private double size;          // 거래량
    private double price;         // 거래 가격
    private LocalDateTime timestamp; // 거래 시간
}