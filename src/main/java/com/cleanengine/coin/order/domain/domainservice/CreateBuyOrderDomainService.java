package com.cleanengine.coin.order.domain.domainservice;

import com.cleanengine.coin.order.domain.BuyOrder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CreateBuyOrderDomainService implements CreateOrderDomainService<BuyOrder> {
    @Override
    public BuyOrder createOrder(String ticker, Integer userId, Boolean isBuyOrder, Boolean isMarketOrder, Double orderSize, Double price, LocalDateTime createdAt, Boolean isBot) {
        if(isMarketOrder){
            return BuyOrder.createMarketBuyOrder(ticker, userId, price, createdAt, isBot);
        }else{
            return BuyOrder.createLimitBuyOrder(ticker, userId, orderSize, price, createdAt, isBot);
        }
    }
}
