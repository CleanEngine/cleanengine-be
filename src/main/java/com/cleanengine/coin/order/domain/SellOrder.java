package com.cleanengine.coin.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "sell_orders")
@Table(name="sell_orders")
@AttributeOverride(name="id", column=@Column(name="sell_order_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SellOrder extends Order implements Comparable<SellOrder> {
    public static SellOrder createMarketSellOrder(String ticker, Integer userId, Double orderSize,
                                                  LocalDateTime createdAt, Boolean isBot) {
        SellOrder sellOrder = new SellOrder();
        sellOrder.ticker = ticker;
        sellOrder.userId = userId;
        sellOrder.state = OrderStatus.WAIT;
        sellOrder.orderSize = orderSize;
        sellOrder.price = null;
        sellOrder.createdAt = createdAt;
        sellOrder.isMarketOrder = true;
        sellOrder.remainingSize = orderSize;
        sellOrder.isBot = isBot;
        return sellOrder;
    }

    public static SellOrder createLimitSellOrder(String ticker, Integer userId, Double orderSize,
                                                 Double price, LocalDateTime createdAt, Boolean isBot) {
        SellOrder sellOrder = new SellOrder();
        sellOrder.ticker = ticker;
        sellOrder.userId = userId;
        sellOrder.state = OrderStatus.WAIT;
        sellOrder.orderSize = orderSize;
        sellOrder.price = price;
        sellOrder.createdAt = createdAt;
        sellOrder.isMarketOrder = false;
        sellOrder.remainingSize = orderSize;
        sellOrder.isBot = isBot;
        return sellOrder;
    }

    @Override
    public int compareTo(SellOrder order) {
        // 지정가 매도 가격 비교
        if(!this.isMarketOrder){
            // 매도 가격이 낮다면 음수가 나와야 함
            int priceCompareResult = Double.compare(this.price, order.price);
            if(priceCompareResult != 0) return priceCompareResult;
        }
        
        // 생성 시간 비교
        
        return this.createdAt.compareTo(order.createdAt);
    }
}
