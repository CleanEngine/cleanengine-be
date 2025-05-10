package com.cleanengine.coin.chart.Dto;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
//실시간 데이터   - 종목 가격 거래량 변동률
public class RealTimeTradeDto {

    private String ticker;
    private Double price;
    private Double size; //거래량
    private Double changeRate; //가격 변동률
}
