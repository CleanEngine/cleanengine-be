package com.cleanengine.coin.order.domain.domainservice;

import com.cleanengine.coin.order.domain.SellOrder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CreateSellOrderDomainService implements CreateOrderDomainService<SellOrder> {
    @Override
    public SellOrder createOrder(String ticker, Integer userId, Boolean isBuyOrder, Boolean isMarketOrder, Double orderSize, Double price, LocalDateTime createdAt, Boolean isBot) {
        if(isMarketOrder){
            return SellOrder.createMarketSellOrder(ticker, userId, orderSize, createdAt, isBot);
        }else{
            return SellOrder.createLimitSellOrder(ticker, userId, orderSize, price, createdAt, isBot);
        }
    }
}
