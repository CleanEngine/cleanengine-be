package com.cleanengine.coin.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.FieldError;
import com.cleanengine.coin.common.error.DomainValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// TODO AttributeOverride를 통해 Annotation 재지정 필요
@Entity(name = "buy_orders")
@Table(name="buy_orders")
@AttributeOverride(name="id", column=@Column(name="buy_order_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class BuyOrder extends Order implements Comparable<BuyOrder> {
    @Column(name="locked_deposit", nullable = false, updatable = false)
    private Double lockedDeposit;

    @Column(name="remaining_deposit", nullable = false)
    private Double remainingDeposit;

    public static BuyOrder createMarketBuyOrder(String ticker, Integer userId, Double price,
                                                LocalDateTime createdAt, Boolean isBot) {
        List<FieldError> errors = new ArrayList<>();
        if(price == null){
            errors.add(new FieldError("BuyOrder", "price", "price cannot be null"));
        }
        handleValidationErrors(errors);

        BuyOrder buyOrder = new BuyOrder();
        buyOrder.ticker = ticker;
        buyOrder.userId = userId;
        buyOrder.state = OrderStatus.WAIT;
        buyOrder.lockedDeposit = price;
        buyOrder.orderSize = null;
        buyOrder.price = null;
        buyOrder.createdAt = createdAt;
        buyOrder.isMarketOrder = true;
        buyOrder.remainingDeposit = buyOrder.lockedDeposit;
        buyOrder.remainingSize = null;
        buyOrder.isBot = isBot;
        return buyOrder;
    }
  
    public static BuyOrder createLimitBuyOrder(String ticker, Integer userId, Double orderSize,
                                  Double price, LocalDateTime createdAt, Boolean isBot) {
        List<FieldError> errors = new ArrayList<>();
        if(orderSize == null){
            errors.add(new FieldError("BuyOrder", "orderSize", "orderSize cannot be null"));
        }
        if(price == null){
            errors.add(new FieldError("BuyOrder", "price", "price cannot be null"));
        }
        handleValidationErrors(errors);

        BuyOrder buyOrder = new BuyOrder();
        buyOrder.ticker = ticker;
        buyOrder.userId = userId;
        buyOrder.state = OrderStatus.WAIT;
        buyOrder.lockedDeposit = orderSize * price;
        buyOrder.orderSize = orderSize;
        buyOrder.price = price;
        buyOrder.createdAt = createdAt;
        buyOrder.isMarketOrder = false;
        buyOrder.remainingDeposit = buyOrder.lockedDeposit;
        buyOrder.remainingSize = orderSize;
        buyOrder.isBot = isBot;
        return buyOrder;
    }

    private static void handleValidationErrors(List<FieldError> errors) {
        if(errors.size() > 0){
            throw new DomainValidationException(
                    "Validation Error occurred Creating BuyOrder", errors);
        }
    }

    @Override
    public int compareTo(BuyOrder order) {
        // 지정가 매수 가격 비교
        if(!this.isMarketOrder){
            // 매수 가격이 높다면 음수가 나와야 함
            int priceCompareResult = -Double.compare(this.price, order.price);
            if(priceCompareResult != 0) return priceCompareResult;
        }

        // 생성 시간 비교
        // 생성 시간이 빠르다면 음수가 나와야 함
        return this.createdAt.compareTo(order.createdAt);
    }
}
