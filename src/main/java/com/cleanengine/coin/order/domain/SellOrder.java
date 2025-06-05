package com.cleanengine.coin.order.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "sell_orders")
@Table(name="sell_orders")
@AttributeOverride(name="id", column=@Column(name="sell_order_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SellOrder extends Order implements Comparable<SellOrder> {
    public static SellOrder createMarketSellOrder(String ticker, Integer userId, Double orderSize,
                                                  LocalDateTime createdAt, Boolean isBot) {
        List<FieldError> errors = new ArrayList<>();
        if(orderSize == null){
            errors.add(new FieldError("SellOrder", "orderSize", "orderSize cannot be null"));
        }

        handleValidationErrors(errors);

        SellOrder sellOrder = new SellOrder(null, ticker, userId, OrderStatus.WAIT, orderSize, null,
                orderSize, createdAt, true, isBot);
        return sellOrder;
    }

    public static SellOrder createLimitSellOrder(String ticker, Integer userId, Double orderSize,
                                                 Double price, LocalDateTime createdAt, Boolean isBot) {
        List<FieldError> errors = new ArrayList<>();
        if(orderSize == null){
            errors.add(new FieldError("SellOrder", "orderSize", "orderSize cannot be null"));
        }
        if(price == null){
            errors.add(new FieldError("SellOrder", "price", "price cannot be null"));
        }

        handleValidationErrors(errors);

        SellOrder sellOrder = new SellOrder(null, ticker, userId, OrderStatus.WAIT, orderSize, price,
                orderSize, createdAt, false, isBot);
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

    protected SellOrder(Long id, String ticker, Integer userId, OrderStatus state, Double orderSize,
                       Double price, Double remainingSize, LocalDateTime createdAt, Boolean isMarketOrder, Boolean isBot) {
        super(id, ticker, userId, state, orderSize, price, remainingSize, createdAt, isMarketOrder, isBot);
    }
}
