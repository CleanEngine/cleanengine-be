package com.cleanengine.coin.order.domain.domainservice;

import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.SellOrder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CreateOrderDomainService {
    public Order createOrder(String ticker, Integer userId, Boolean isBuyOrder, Boolean isMarketOrder,
                             Double orderSize, Double price, LocalDateTime createdAt, Boolean isBot){
        if(isBuyOrder){
            if(isMarketOrder){
                return BuyOrder.createMarketBuyOrder(ticker, userId, price, createdAt, isBot);
            }else{
                return BuyOrder.createLimitBuyOrder(ticker, userId, orderSize, price, createdAt, isBot);
            }
        }else{
            if(isMarketOrder){
                return SellOrder.createMarketSellOrder(ticker, userId, price, createdAt, isBot);
            }else{
                return SellOrder.createLimitSellOrder(ticker, userId, orderSize, price, createdAt, isBot);
            }
        }
    }
}
