package com.cleanengine.coin.mypage.dto;

import com.cleanengine.coin.order.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CompletedOrderDto {
    private Boolean isBuy;
    private OrderStatus state;
    private Long id;
    private String ticker;
    private String name;
    private Double price;
    private Double size;
    private LocalDateTime tradeTime;
}
